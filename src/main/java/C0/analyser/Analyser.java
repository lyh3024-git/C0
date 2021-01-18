package C0.analyser;

import C0.error.*;
import C0.instruction.Instruction;
import C0.instruction.Operation;
import C0.tokenizer.Token;
import C0.tokenizer.TokenType;
import C0.tokenizer.Tokenizer;
import C0.struct.*;
import C0.util.Pos;
import C0.util.Type;
import org.checkerframework.checker.units.qual.A;

import java.util.*;

public final class Analyser {

    Tokenizer tokenizer;

    /** 符号表 */
    List<Symbol> symbolTable=new ArrayList<>();

    /** 函数表 */
    List<FunctionDef> functionTable=new ArrayList<>();

    /** 全局表 */
    List<GlobalDef> globalTable=new ArrayList<>();

    /** 函数参数表 */
    List<Parameter> parameterList=new ArrayList<>();

    /** 指令集 */
    List<Instruction> instructionList=new ArrayList<>();

    /** 全局指令集 */
    List<Instruction> globalInstructionList=new ArrayList<>();

    /** 符号栈 */
    Stack<TokenType> stack=new Stack<>();

    /** 全局变量的偏移 */
    int globalOffset = 0;

    /** 局部变量的偏移 */
    int localOffset = 0;

    /** 当前所在的层次 */
    int level = 0;

    /** 表示函数是否有返回值 */
    int paramsOffset=0;

    /**
     * 表示函数的顺序编号
     * 在使用call指令时使用
     * 函数编号为0的为_start函数
     */
    int functionID = 1;

    /** 当前偷看的 token */
    Token peekedToken = null;

    public List<GlobalDef> getGlobalTable() {
        return globalTable;
    }

    public List<FunctionDef> getFunctionTable() {
        return functionTable;
    }

    /**
     * 查看下一个 Token
     * 
     * @return
     * @throws TokenizeError
     */
    private Token peek() throws TokenizeError {
        if (peekedToken == null) {
            peekedToken = tokenizer.nextToken();
        }
        return peekedToken;
    }

    /**
     * 获取下一个 Token
     * 
     * @return
     * @throws TokenizeError
     */
    private Token next() throws TokenizeError {
        if (peekedToken != null) {
            var token = peekedToken;
            peekedToken = null;
            return token;
        } else {
            return tokenizer.nextToken();
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则返回 true
     * 
     * @param tt
     * @return
     * @throws TokenizeError
     */
    private boolean check(TokenType tt) throws TokenizeError {
        var token = peek();
        return token.getTokenType() == tt;
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回这个 token
     * 
     * @param tt 类型
     * @return 如果匹配则返回这个 token，否则返回 null
     * @throws TokenizeError
     */
    private Token nextIf(TokenType tt) throws TokenizeError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            return null;
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回，否则抛出异常
     * 
     * @param tt 类型
     * @return 这个 token
     * @throws CompileError 如果类型不匹配
     */
    private Token expect(TokenType tt) throws CompileError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            throw new ExpectedTokenError(tt, token);
        }
    }

    public Analyser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public List<Instruction> analyse() throws CompileError {
        analyseProgram();
        return instructionList;
    }

    /**
     * 分析程序
     * program -> decl_stmt* function*
     * @throws CompileError
     */
    private void analyseProgram() throws CompileError {
        while(check(TokenType.LET_KW)||check(TokenType.CONST_KW)||check(TokenType.FN_KW)){
            analyseItem();
        }
        FunctionDef mainFunction=AuxiliaryFunction.getMainFunction(functionTable);
        if(mainFunction==null){
            throw new AnalyzeError(ErrorCode.NoMain);
        }
        //添加_start
        globalTable.add(new GlobalDef("_start",1,"_start".toCharArray()));
        if(mainFunction.getType()==Type.VOID){
            globalInstructionList.add(new Instruction(Operation.stackalloc,0,4));
            globalInstructionList.add(new Instruction(Operation.call,functionID-1,4));
        }
        else{
            //如果main函数的返回值非空则分配一个slot来存放返回值
            globalInstructionList.add(new Instruction(Operation.stackalloc,1,4));
            globalInstructionList.add(new Instruction(Operation.call,functionID-1,4));
            globalInstructionList.add(new Instruction(Operation.popn,1,4));
        }
        functionTable.add(new FunctionDef(globalOffset,0,0,0,globalInstructionList,"_start",null,Type.VOID,0));
        globalOffset++;
    }

    /**
     * item -> function | decl_stmt
     * @throws CompileError
     */
    private void analyseItem() throws CompileError {
        if(check(TokenType.LET_KW)||check(TokenType.CONST_KW)){
            analyseDecStmt();
        }
        else{
            analyseFunction();
        }
    }

    /**
     * 分析表达式
     * expr ->
     *       operator_expr
     *     | negate_expr
     *     | assign_expr
     *     | as_expr
     *     | call_expr
     *     | literal_expr
     *     | ident_expr
     *     | group_expr
     * @return
     * @throws CompileError
     */
    private Type analyseExpression() throws CompileError{
        Type type1;
        if(check(TokenType.MINUS)){
            type1=analyseNegateExpression();
        }
        else if(check(TokenType.IDENT)){
            type1=analyseAssign_Call_IdentExpression();
        }
        else if(check(TokenType.UINT_LITERAL)||check(TokenType.STRING_LITERAL)){
            type1=analyseLiteralExpression();
        }
        else if(check(TokenType.L_PAREN)){
            type1=analyseGroupExpression();
        }
        else{
            throw new ExpectedTokenError(List.of(TokenType.MINUS, TokenType.IDENT, TokenType.UINT_LITERAL,TokenType.STRING_LITERAL,TokenType.L_PAREN),next());
        }
        while(AuxiliaryFunction.isBinaryOperation(peek().getTokenType())){
            Token operator=next();
            if(!stack.empty()){
                int first=OperatorPrecedence.getOffset(stack.peek());
                int second=OperatorPrecedence.getOffset(operator.getTokenType());
                if(OperatorPrecedence.getPriority(first,second)>0){
                    Instruction.addInstruction(stack.pop(),instructionList);
                }
            }
            stack.push(operator.getTokenType());
            Type type2=analyseExpression();
            if(type1!=type2){
                throw new AnalyzeError(ErrorCode.TypeError,operator.getStartPos());
            }
        }
        return type1;
    }

    /**
     * negate_expr -> '-' expr
     *
     * @return
     * @throws CompileError
     */
    private Type analyseNegateExpression() throws CompileError{
        next();
        Type type=analyseExpression();
        if(type==Type.INT){
            instructionList.add(new Instruction(Operation.neg));
        }
        else {
            throw new AnalyzeError(ErrorCode.TypeError,peek().getStartPos());
        }
        return type;
    }

    /**
     * l_expr -> IDENT
     * assign_expr -> l_expr '=' expr
     *
     * call_param_list -> expr (',' expr)*
     * call_expr -> IDENT '(' call_param_list? ')'
     *
     * ident_expr -> IDENT
     *
     * @return
     * @throws CompileError
     */
    private Type analyseAssign_Call_IdentExpression()throws CompileError{
        Type l_type;
        Token l_token=expect(TokenType.IDENT);
        //赋值表达式
        if(check(TokenType.ASSIGN)){
            next();
            //是否是定义的变量
            Symbol symbol=AuxiliaryFunction.isDefinedSymbol(symbolTable,level,l_token.getValueString());
            //是否是函数的参数
            Parameter parameter=AuxiliaryFunction.isParameter(parameterList,l_token.getValueString());
            if(symbol!=null){
                l_type=symbol.getType();
                //如果是常量
                if(symbol.isConstant()){
                    throw new AnalyzeError(ErrorCode.AssignToConstant,l_token.getStartPos());
                }
                //如果变量类型为void
                if(symbol.getType()==Type.VOID){
                    throw new AnalyzeError(ErrorCode.TypeError,l_token.getStartPos());
                }
                //如果是全局变量
                if(symbol.getLevel()==0){
                    instructionList.add(new Instruction(Operation.globa,symbol.getOffset(),4));
                }
                //如果是局部变量
                else{
                    instructionList.add(new Instruction(Operation.loca,symbol.getOffset(),4));
                }
            }
            else if(parameter!=null){
                l_type=parameter.getType();
                //如果参数的类型为void
                if(l_type==Type.VOID){
                    throw new AnalyzeError(ErrorCode.InvalidAssignment,l_token.getStartPos());
                }
                instructionList.add(new Instruction(Operation.arga,paramsOffset+parameter.getOffset(),4));
            }
            else {
                throw new AnalyzeError(ErrorCode.NotDeclared,l_token.getStartPos());
            }

            Type r_type=analyseExpression();
            if(l_type!=r_type){
                throw new AnalyzeError(ErrorCode.TypeError,l_token.getStartPos());
            }

            while(!stack.empty()){
                Instruction.addInstruction(stack.pop(),instructionList);
            }

            instructionList.add(new Instruction(Operation.store));
            l_type=Type.VOID;
        }
        //函数调用表达式
        else if(check(TokenType.L_PAREN)){
            next();

            stack.push(TokenType.L_PAREN);

            FunctionDef function=AuxiliaryFunction.isDefinedFunction(functionTable,l_token.getValueString());

            Instruction instruction;//call函数调用要添加在最后

            //库函数
            if(AuxiliaryFunction.isLibraryFunction(l_token.getValueString())){
                globalTable.add(new GlobalDef(l_token.getValueString(),1,l_token.getValueString().toCharArray()));
                instruction=new Instruction(Operation.callname,globalOffset,4);
                l_type=AuxiliaryFunction.getTypeofLibrary(l_token.getValueString());
                globalOffset++;
            }
            else if(function!=null){
                instruction=new Instruction(Operation.call,function.getFunctionID(),4);
                l_type=function.getType();
            }
            else{
                throw new AnalyzeError(ErrorCode.NotDeclared,l_token.getStartPos());
            }

            if(AuxiliaryFunction.hasReturn(l_token.getValueString(),functionTable)){
                instructionList.add(new Instruction(Operation.stackalloc,1,4));
            }
            else {
                instructionList.add(new Instruction(Operation.stackalloc,0,4));
            }

            //分析参数列表
            if(check(TokenType.L_PAREN)||check(TokenType.IDENT)||check(TokenType.MINUS)||check(TokenType.UINT_LITERAL)||check(TokenType.STRING_LITERAL)){
                analyseCallParamList(l_token.getValueString());
            }
            expect(TokenType.R_PAREN);

            while(stack.peek()!=TokenType.L_PAREN){
                Instruction.addInstruction(stack.pop(),instructionList);
            }
            stack.pop();

            instructionList.add(instruction);
        }
        //标识符表达式
        else {
            //是否是定义的变量
            Symbol symbol=AuxiliaryFunction.isDefinedSymbol(symbolTable,level,l_token.getValueString());
            //是否是函数的参数
            Parameter parameter=AuxiliaryFunction.isParameter(parameterList,l_token.getValueString());
            if(symbol!=null){
                //判断是否是全局变量
                if(symbol.getLevel()==0){
                    instructionList.add(new Instruction(Operation.globa,symbol.getOffset(),4));
                }
                else{
                    instructionList.add(new Instruction(Operation.loca,symbol.getOffset(),4));
                }
                instructionList.add(new Instruction(Operation.load));
                l_type=symbol.getType();
            }
            else if(parameter!=null){
                instructionList.add(new Instruction(Operation.arga,paramsOffset+parameter.getOffset(),4));
                instructionList.add(new Instruction(Operation.load));
                l_type=parameter.getType();
            }
            else{
                throw new AnalyzeError(ErrorCode.NotDeclared,l_token.getStartPos());
            }
        }
        return l_type;
    }

    /**
     * call_param_list -> expr (',' expr)*
     * 同时判断参数列表与函数的参数列表是否一一对应
     * */
    private int analyseCallParamList(String name) throws CompileError{
        List<Type> TypeList = new ArrayList<>();
        int count = 0;
        Type type = analyseExpression();
        TypeList.add(type);
        while (!stack.empty() && stack.peek() != TokenType.L_PAREN) {
            Instruction.addInstruction(stack.pop(),instructionList);
        }
        count++;

        while(nextIf(TokenType.COMMA)!=null){
            type = analyseExpression();
            TypeList.add(type);
            while (!stack.empty() && stack.peek() != TokenType.L_PAREN) {
                Instruction.addInstruction(stack.pop(),instructionList);
            }
            count++;
        }

        List<Type> ParamTypeList = AuxiliaryFunction.TypeReturn(name, functionTable);
        if(ParamTypeList.size()==TypeList.size()){
            for (int i=0 ;i<TypeList.size();i++){
                if(TypeList.get(i)!=ParamTypeList.get(i)){
                    throw new AnalyzeError(ErrorCode.ParamError);
                }
            }
        }else{
            throw new AnalyzeError(ErrorCode.ParamError);
        }

        return count;
    }

    /**
     * literal_expr -> UINT_LITERAL | STRING_LITERAL
     *
     * @return
     * @throws CompileError
     */
    private Type analyseLiteralExpression() throws CompileError{
        Type type;
        Token token=next();
        if(token.getTokenType()==TokenType.UINT_LITERAL){
            instructionList.add(new Instruction(Operation.push,(Integer) token.getValue(),4));
            type=Type.INT;
        }
        else{
            globalTable.add(new GlobalDef(token.getValueString(),1));
            instructionList.add(new Instruction(Operation.push,globalOffset,8));
            globalOffset++;
            type=Type.STRING;
        }
        return type;
    }

    /**
     * group_expr -> '(' expr ')'
     *
     * @return
     * @throws CompileError
     */
    private Type analyseGroupExpression() throws CompileError{
        expect(TokenType.L_PAREN);
        stack.push(TokenType.L_PAREN);
        Type type=analyseExpression();
        expect(TokenType.R_PAREN);

        while(stack.peek()!=TokenType.L_PAREN){
            Instruction.addInstruction(stack.pop(),instructionList);
        }

        stack.pop();
        return type;
    }

    /**
     * 分析语句
     * stmt ->
     *       expr_stmt
     *     | decl_stmt
     *     | if_stmt
     *     | while_stmt
     *     | return_stmt
     *     | block_stmt
     *     | empty_stmt
     * @throws CompileError
     */
    private void analyseStatement(FunctionDef function) throws CompileError{
        if(AuxiliaryFunction.isExpression(peek())){
            analyseExpression();

            while(!stack.empty()){
                Instruction.addInstruction(stack.pop(),instructionList);
            }
            expect(TokenType.SEMICOLON);
        }
        else if(check(TokenType.LET_KW)||check(TokenType.CONST_KW)){
            analyseDecStmt();
        }
        else if(check(TokenType.IF_KW)){
            analyseIfStmt(function);
        }
        else if(check(TokenType.WHILE_KW)){
            analyseWhileStmt(function);
        }
        else if(check(TokenType.RETURN_KW)){
            analyseReturnStmt(function);
        }
        else if(check(TokenType.L_BRACE)){
            analyseBlockStmt(function);
        }
        else if(check(TokenType.SEMICOLON)){
            analyseEmptyStmt();
        }
        else {
            throw new AnalyzeError(ErrorCode.InvalidInput,peek().getStartPos());
        }
    }

    /**
     * let_decl_stmt -> 'let' IDENT ':' ty ('=' expr)? ';'
     * const_decl_stmt -> 'const' IDENT ':' ty '=' expr ';'
     * decl_stmt -> let_decl_stmt | const_decl_stmt
     *
     * @throws CompileError
     */
    private void analyseDecStmt() throws CompileError{
        //变量
        if(check(TokenType.LET_KW)){
            next();
            Token ident=expect(TokenType.IDENT);

            expect(TokenType.COLON);
            //获取变量的类型
            Type l_type=AuxiliaryFunction.getType(next());
            if(l_type==Type.VOID){
                throw new AnalyzeError(ErrorCode.TypeError,ident.getStartPos());
            }

            //全局变量
            if(level==0){
                if(AuxiliaryFunction.isDefinedGlobal(globalTable,ident.getValueString())){
                    throw new AnalyzeError(ErrorCode.DuplicateDeclaration,ident.getStartPos());
                }
                globalTable.add(new GlobalDef((String)ident.getValue(),0));
                if(check(TokenType.ASSIGN)){
                    symbolTable.add(new Symbol(false,true,globalOffset,ident.getValueString(),l_type,level));
                }
                else{
                    symbolTable.add(new Symbol(false,false,globalOffset,ident.getValueString(),l_type,level));
                }
            }
            //局部变量
            else{
                if(AuxiliaryFunction.isSameLevelDefinedSymbol(symbolTable,level,ident.getValueString())){
                    throw new AnalyzeError(ErrorCode.DuplicateDeclaration,ident.getStartPos());
                }

                //局部变量是否能和函数参数重名

                if(check(TokenType.ASSIGN)){
                    symbolTable.add(new Symbol(false,true,localOffset,ident.getValueString(),l_type,level));
                }
                else{
                    symbolTable.add(new Symbol(false,false,localOffset,ident.getValueString(),l_type,level));
                }
            }
            if(check(TokenType.ASSIGN)) {
                next();
                if (level == 0) {
                    globalInstructionList.add(new Instruction(Operation.globa, globalOffset,4));
                } else {
                    instructionList.add(new Instruction(Operation.loca,localOffset,4));
                }

                Type r_type = analyseExpression();
                if (l_type != r_type) {
                    throw new AnalyzeError(ErrorCode.TypeError, peek().getStartPos());
                }

                while(!stack.empty()){
                    Instruction.addInstruction(stack.pop(),instructionList);
                }

                instructionList.add(new Instruction(Operation.store));
            }
            expect(TokenType.SEMICOLON);
        }
        //常量
        else{
            next();
            Token ident=expect(TokenType.IDENT);

            expect(TokenType.COLON);
            //获取变量的类型
            Type l_type=AuxiliaryFunction.getType(next());
            if(l_type==Type.VOID){
                throw new AnalyzeError(ErrorCode.TypeError,ident.getStartPos());
            }

            expect(TokenType.ASSIGN);

            //全局常量
            if (level == 0) {
                if(AuxiliaryFunction.isDefinedGlobal(globalTable,ident.getValueString())){
                    throw new AnalyzeError(ErrorCode.DuplicateDeclaration,ident.getStartPos());
                }
                symbolTable.add(new Symbol(true,true,globalOffset,ident.getValueString(),l_type,level));
                globalTable.add(new GlobalDef(ident.getValueString(),1));
                globalInstructionList.add(new Instruction(Operation.globa, globalOffset,4));
            }
            //局部常量
            else {
                if(AuxiliaryFunction.isSameLevelDefinedSymbol(symbolTable,level,ident.getValueString())){
                    throw new AnalyzeError(ErrorCode.DuplicateDeclaration,ident.getStartPos());
                }

                symbolTable.add(new Symbol(true,true,globalOffset,ident.getValueString(),l_type,level));
                instructionList.add(new Instruction(Operation.loca,localOffset,4));
            }

            Type r_type = analyseExpression();
            if (l_type != r_type) {
                throw new AnalyzeError(ErrorCode.TypeError, peek().getStartPos());
            }

            while (!stack.empty()){
                Instruction.addInstruction(stack.pop(),instructionList);
            }

            instructionList.add(new Instruction(Operation.store));

            expect(TokenType.SEMICOLON);

        }

        if(level==0){
            globalOffset++;
        }
        else{
            localOffset++;
        }
    }

    /**
     * if_stmt -> 'if' expr block_stmt ('else' (block_stmt | if_stmt))?
     * //              ^~~~ ^~~~~~~~~~         ^~~~~~~~~~~~~~~~~~~~~~
     * //              |     if_block           else_block
     * //              condition
     * @param function
     * @throws CompileError
     */
    private void analyseIfStmt(FunctionDef function) throws CompileError{
        next();
        analyseExpression();

        while(!stack.empty()){
            Instruction.addInstruction(stack.pop(),instructionList);
        }

        instructionList.add(new Instruction(Operation.br_true,1,4));
        Instruction jump_Instruction1=new Instruction(Operation.br,0,4);
        instructionList.add(jump_Instruction1);
        //记录下跳转开始前的位置
        int if_StartPos=instructionList.size();

        analyseBlockStmt(function);

        Instruction jump_Instruction2=new Instruction(Operation.br,0,4);
        instructionList.add(jump_Instruction2);
        int else_StartPos=instructionList.size();

        int jump_size1=instructionList.size()-if_StartPos;//表示if不成立时该跳转到offset
        jump_Instruction1.setX(jump_size1);


        if(check(TokenType.ELSE_KW)){
            next();
            if(check(TokenType.IF_KW)){
                analyseIfStmt(function);
            }
            else{
                analyseBlockStmt(function);
                instructionList.add(new Instruction(Operation.br,0,4));
            }
        }

        jump_Instruction2.setX(instructionList.size()-else_StartPos);
    }

    /**
     * while_stmt -> 'while' expr block_stmt
     * //                    ^~~~ ^~~~~~~~~~while_block
     * //                     condition
     * @param function
     * @throws CompileError
     */
    private void analyseWhileStmt(FunctionDef function) throws CompileError{
        next();

        int Pos1=instructionList.size();

        analyseExpression();

        while(!stack.empty()){
            Instruction.addInstruction(stack.pop(),instructionList);
        }

        instructionList.add(new Instruction(Operation.br_true,1,4));
        Instruction jump_instruction1=new Instruction(Operation.br,0,4);
        instructionList.add(jump_instruction1);

        int Pos2=instructionList.size();

        analyseBlockStmt(function);

        Instruction jump_instruction2=new Instruction(Operation.br,0,4);
        instructionList.add(jump_instruction2);

        int Pos3=instructionList.size();

        jump_instruction1.setX(Pos3-Pos2);
        jump_instruction2.setX(Pos1-Pos3);
    }

    /**
     * return_stmt -> 'return' expr? ';'
     * @throws CompileError
     */
    private void analyseReturnStmt(FunctionDef function) throws CompileError{
        next();
        if(AuxiliaryFunction.isExpression(peek())){
            if(function.getType()==Type.INT||function.getType()==Type.DOUBLE){
                instructionList.add(new Instruction(Operation.arga,0,4));
                Type type=analyseExpression();
                if(function.getType()!=type){
                    throw new AnalyzeError(ErrorCode.TypeError);
                }
                while(!stack.empty()){
                    Instruction.addInstruction(stack.pop(),instructionList);
                }
                instructionList.add(new Instruction(Operation.store));
            }
            else{
                throw new AnalyzeError(ErrorCode.TypeError);
            }
        }
        expect(TokenType.SEMICOLON);
        while(!stack.empty()){
            Instruction.addInstruction(stack.pop(),instructionList);
        }
        instructionList.add(new Instruction(Operation.ret));
    }

    /**
     * block_stmt -> '{' stmt* '}'
     * @throws CompileError
     */
    private void analyseBlockStmt(FunctionDef function) throws CompileError{
        next();
        //进入语句块,层数++
        level++;
        while(AuxiliaryFunction.isStatement(peek())){
            analyseStatement(function);
        }
        expect(TokenType.R_BRACE);

        //删除该层的局部变量
        AuxiliaryFunction.clearSameLevelSymbol(symbolTable,level);

        level--;
    }

    /**
     * empty_stmt -> ';'
     * @throws CompileError
     */
    private void analyseEmptyStmt() throws CompileError{
        expect(TokenType.SEMICOLON);
    }

    /**
     * 分析函数
     * function_param -> 'const'? IDENT ':' ty
     * function_param_list -> function_param (',' function_param)*
     * function -> 'fn' IDENT '(' function_param_list? ')' '->' ty block_stmt
     * //               ^~~~      ^~~~~~~~~~~~~~~~~~~~          ^~ ^~~~~~~~~~
     * //               |              |                        |  |
     * //               function_name  param_list     return_type  function_body
     * @throws CompileError
     */
    private void analyseFunction() throws CompileError{
        paramsOffset=0;
        localOffset=0;
        instructionList.clear();
        parameterList.clear();
        expect(TokenType.FN_KW);
        //name保存函数名的IDENT
        Token ident=expect(TokenType.IDENT);
        expect(TokenType.L_PAREN);
        if(AuxiliaryFunction.isDefinedGlobal(globalTable,ident.getValueString())){
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration,ident.getStartPos());
        }

        FunctionDef function=new FunctionDef(0,ident.getValueString(),functionID);

        //分析参数
        int paramSize=0;//表示参数的个数
        if(check(TokenType.CONST_KW)||check(TokenType.IDENT)){
            if(check(TokenType.CONST_KW)){
                next();
            }
            Token paramName=expect(TokenType.IDENT);
            expect(TokenType.COLON);
            Type paramType=AuxiliaryFunction.getType(next());
            parameterList.add(new Parameter(paramName.getValueString(),paramType,paramSize));
            paramSize++;
            while(check(TokenType.COMMA)){
                next();
                if(check(TokenType.CONST_KW)){
                    next();
                }
                paramName=expect(TokenType.IDENT);
                //判断参数是否重名
                for(Parameter parameter:parameterList){
                    if(parameter.getName().equals(paramName.getValueString())){
                        throw new AnalyzeError(ErrorCode.DuplicateDeclaration,paramName.getStartPos());
                    }
                }
                expect(TokenType.COLON);
                paramType=AuxiliaryFunction.getType(next());
                parameterList.add(new Parameter(paramName.getValueString(),paramType,paramSize));
                paramSize++;
            }
            List<Parameter> tmp=new ArrayList<>(parameterList);
            function.setParameters(tmp);
            function.setParam_slots(paramSize);
        }

        expect(TokenType.R_PAREN);
        expect(TokenType.ARROW);

        Type type=AuxiliaryFunction.getType(next());
        function.setType(type);
        if(type==Type.VOID){
            function.setReturn_slots(0);
        }
        else{
            paramsOffset=1;
            function.setReturn_slots(1);
        }

        functionTable.add(function);

        analyseBlockStmt(function);

        if(function.getType()==Type.VOID){
            instructionList.add(new Instruction(Operation.ret));
        }
        globalTable.add(new GlobalDef((String)function.getName(),1,function.getName().toCharArray()));


        function.setLoc_slots(localOffset);
        function.setOffset(globalOffset);
        List<Instruction> tmp = new ArrayList<>(instructionList);
        function.setInstructions(tmp);

        globalOffset++;
        functionID++;

    }
}
