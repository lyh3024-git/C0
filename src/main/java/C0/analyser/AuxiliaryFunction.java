package C0.analyser;

import C0.error.AnalyzeError;
import C0.error.CompileError;
import C0.error.ErrorCode;
import C0.struct.FunctionDef;
import C0.struct.Parameter;
import C0.struct.Symbol;
import C0.tokenizer.Token;
import C0.tokenizer.TokenType;
import C0.util.Type;

import java.awt.datatransfer.FlavorEvent;
import java.util.List;

//这里存放语法分析需要用到的辅助函数
public class AuxiliaryFunction {

    /**
     * 判断Token是否为二元操作符
     */
    public static boolean isBinaryOperation(TokenType tokenType){
        return tokenType == TokenType.MINUS || tokenType == TokenType.PLUS || tokenType == TokenType.MUL ||
                tokenType == TokenType.DIV || tokenType == TokenType.EQ || tokenType == TokenType.NEQ ||
                tokenType == TokenType.LT || tokenType == TokenType.GT || tokenType == TokenType.LE ||
                tokenType == TokenType.GE;
    }

    /**
     * 判断是否是当前层定义了的变量
     * @param symbolTable
     * @param level
     * @param name
     * @return
     */
    public static boolean isSameLevelDefinedSymbol(List<Symbol> symbolTable,int level,String name){
        for(Symbol symbol:symbolTable){
            if(symbol.getLevel()==level&& symbol.getName().equals(name))
                return true;
        }
        return false;
    }

    /**
     * 判断是否是定义了的变量
     * @param symbolTable
     * @param level
     * @param name
     * @return
     */
    public static Symbol isDefinedSymbol(List<Symbol> symbolTable,int level,String name){
        for(Symbol symbol:symbolTable){
            if(symbol.getName().equals(name)){
                if(symbol.getLevel()<=level){
                    return symbol;
                }
            }
        }
        return null;
    }

    /**
     * 判断是否是函数的参数
     * @param parameterList
     * @param name
     * @return
     */
    public static Parameter isParameter(List<Parameter> parameterList,String name){
        for(Parameter parameter:parameterList){
            if(parameter.getName().equals(name)){
                return parameter;
            }
        }
        return null;
    }

    /**
     * 判断函数是否是库函数
     * @param name
     * @return
     */
    public static boolean isLibraryFunction(String name){
        return name.equals("getint")||name.equals("getdouble")||name.equals("getchar")||
                name.equals("putint")||name.equals("putdouble")||name.equals("putchar")||
                name.equals("putstr")||name.equals("putln");
    }

    /**
     * 判断类型
     * @param token
     * @return
     * @throws CompileError
     */
    public static Type getType(Token token) throws CompileError{
        if(token.getTokenType()!=TokenType.IDENT){
            throw new AnalyzeError(ErrorCode.TypeError,token.getStartPos());
        }
        if(token.getValue().equals("int")){
            return Type.INT;
        }
        else if(token.getValue().equals("void")){
            return Type.VOID;
        }
        else{
            throw new AnalyzeError(ErrorCode.InvalidInput,token.getStartPos());
        }
    }

    /**
     * 判断是否是表达式
     * @param token
     * @return
     */
    public static boolean isExpression(Token token){
        TokenType tokenType=token.getTokenType();
        return tokenType==TokenType.MINUS||tokenType==TokenType.IDENT||tokenType==TokenType.L_PAREN||
                tokenType==TokenType.UINT_LITERAL||tokenType==TokenType.STRING_LITERAL;
    }

    /**
     * 判断是否是语句
     * @param token
     * @return
     */
    public static boolean isStatement(Token token){
        TokenType tokenType=token.getTokenType();
        return tokenType == TokenType.MINUS || tokenType == TokenType.IDENT || tokenType == TokenType.L_PAREN ||
                tokenType == TokenType.UINT_LITERAL || tokenType == TokenType.STRING_LITERAL || tokenType == TokenType.LET_KW ||
                tokenType == TokenType.CONST_KW || tokenType == TokenType.IF_KW || tokenType == TokenType.WHILE_KW ||
                tokenType == TokenType.RETURN_KW || tokenType == TokenType.L_BRACE || tokenType == TokenType.SEMICOLON;
    }

    /**
     * 判断函数列表中是否存在main函数
     * @param functionTable
     * @return
     */
    public static FunctionDef getMainFunction(List<FunctionDef> functionTable){
        for(FunctionDef function:functionTable){
            if(function.getName().equals("main")){
                return function;
            }
        }
        return null;
    }

    /**
     * 判断是否是函数名，用于判断变量名是否和函数名重名
     * @param functionTable
     * @param name
     * @return
     */
    public static boolean isFunctionName(List<FunctionDef> functionTable,String name){
        for(FunctionDef functionDef:functionTable){
            if(name.equals(functionDef.getName())){
                return true;
            }
        }
        return false;
    }

    /**
     * 清除当前层的局部变量
     * @param symbolTable
     * @param level
     */
    public static void clearSameLevelSymbol(List<Symbol> symbolTable,int level){
        symbolTable.removeIf(symbol -> symbol.getLevel() == level);
    }

    /**
     * 判断是否是定义了的函数
     * @param functionTable
     * @param name
     * @return
     */
    public static FunctionDef isDefinedFunction(List<FunctionDef> functionTable,String name){
        for(FunctionDef functionDef:functionTable){
            if(functionDef.getName().equals(name)){
                return functionDef;
            }
        }
        return null;
    }

    /**
     * 获取库函数的返回值类型
     * @param name
     * @return
     */
    public static Type getTypeofLibrary(String name){
        if(name.equals("getint")||name.equals("getchar")){
            return Type.INT;
        }
        else if(name.equals("getdouble")){
            return Type.DOUBLE;
        }
        else{
            return Type.VOID;
        }
    }

    /**
     * 获取库函数的参数类型
     * @param name
     * @return
     */
    public static Type getParamTypeofLibrary(String name){
        if(name.equals("putint")||name.equals("putchar")||name.equals("putstr")){
            return Type.INT;
        }
        else if(name.equals("putdouble")){
            return Type.DOUBLE;
        }
        else {
            return null;
        }
    }
}
