//package C0.tokenizer;
//
//import C0.error.ErrorCode;
//import C0.error.TokenizeError;
//import C0.util.Pos;
//
//public class Tokenizer {
//
//    private StringIter it;
//
//    public Tokenizer(StringIter it) {
//        this.it = it;
//    }
//
//    // 这里本来是想实现 Iterator<Token> 的，但是 Iterator 不允许抛异常，于是就这样了
//    /**
//     * 获取下一个 Token
//     *
//     * @return
//     * @throws TokenizeError 如果解析有异常则抛出
//     */
//    public Token nextToken() throws TokenizeError {
//        it.readAll();
//
//        // 跳过之前的所有空白字符
//        skipSpaceCharacters();
//
//        if (it.isEOF()) {
//            return new Token(TokenType.EOF, "", it.currentPos(), it.currentPos());
//        }
//
//        char peek = it.peekChar();
//        if (Character.isDigit(peek)) {
//            return lexUInt();
//        } else if (Character.isAlphabetic(peek)||peek=='_') {
//            return lexIdentOrKeyword();
//        } else if(peek=='\"'){
//            return lexStringLiteral();
//        }else {
//            return lexOperatorOrUnknown();
//        }
//    }
//
//    //无符号整数
//    private Token lexUInt() throws TokenizeError {
//        Pos startPos=it.currentPos();
//        StringBuilder digitStr=new StringBuilder();
//        while(!it.isEOF()&&Character.isDigit(it.peekChar())){
//            digitStr.append(it.nextChar());
//        }
//        try{
//            int digit=Integer.parseInt(digitStr.toString());
//            return new Token(TokenType.UINT_LITERAL,digit,startPos,it.currentPos());
//        }catch (Exception e){
//            throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
//        }
//    }
//
//    //关键字或者标识符
//    private Token lexIdentOrKeyword() throws TokenizeError {
//        Pos startPos=it.currentPos();
//        StringBuilder identStr=new StringBuilder();
//        while(!it.isEOF()&&(Character.isDigit(it.peekChar())||Character.isLetter(it.peekChar())||it.peekChar()=='_')){
//            identStr.append(it.nextChar());
//        }
//        String ident=identStr.toString();
//        TokenType tokenType;
//        switch (ident){
//            case "fn":
//                tokenType=TokenType.FN_KW;
//                break;
//            case "let":
//                tokenType=TokenType.LET_KW;
//                break;
//            case "const":
//                tokenType=TokenType.CONST_KW;
//                break;
//            case "as":
//                tokenType=TokenType.AS_KW;
//                break;
//            case "while":
//                tokenType=TokenType.WHILE_KW;
//                break;
//            case "if":
//                tokenType=TokenType.IF_KW;
//                break;
//            case "else":
//                tokenType=TokenType.ELSE_KW;
//                break;
//            case "return":
//                tokenType=TokenType.RETURN_KW;
//                break;
//            case "break":
//                tokenType=TokenType.BREAK_KW;
//                break;
//            case "continue":
//                tokenType=TokenType.CONTINUE_KW;
//                break;
//            default:
//                tokenType=TokenType.IDENT;
//                break;
//        }
//        return new Token(tokenType,ident,startPos,it.currentPos());
//    }
//
//    //字符串常量
//    private Token lexStringLiteral() throws TokenizeError{
//        Pos startPos=it.currentPos();
//        StringBuilder strLiteral=new StringBuilder();
//        //将入口处判断的"加入字符串常量
//        strLiteral.append(it.nextChar());
//        while(!it.isEOF()){
//            //判断是否是字符串常量结尾
//            if(it.peekChar()=='"'){
//                strLiteral.append(it.nextChar());
//                break;
//            }
//            if(it.peekChar()=='\\'){
//                it.nextChar();
//                char peek=it.peekChar();
//                if(peek=='\\'||peek=='\''||peek=='\"'||peek=='\n'||peek=='\t'||peek=='\r'){
//                    strLiteral.append(it.nextChar());
//                }
//                else{
//                    throw new TokenizeError(ErrorCode.InvalidInput,it.nextPos());
//                }
//            }
//            else {
//                strLiteral.append(it.nextChar());
//            }
//        }
//        return new Token(TokenType.STRING_LITERAL,strLiteral.toString(),startPos,it.currentPos());
//    }
//
//    //运算符
//    private Token lexOperatorOrUnknown() throws TokenizeError {
//        Pos startPos;
//        switch (it.peekChar()) {
//            case '+':
//                it.nextChar();
//                return new Token(TokenType.PLUS, '+', it.previousPos(), it.currentPos());
//
//            case '-':
//                it.nextChar();
//                //读到“-”判断是“-”还是“->”
//                startPos=it.previousPos();
//                if(it.peekChar()=='>'){
//                    it.nextChar();
//                    return new Token(TokenType.ARROW,"->",startPos,it.currentPos());
//                }
//                return new Token(TokenType.MINUS, '-', it.previousPos(), it.currentPos());
//
//            case '*':
//                it.nextChar();
//                return new Token(TokenType.MUL, '*', it.previousPos(), it.currentPos());
//
//            case '/':
//                it.nextChar();
//                return new Token(TokenType.DIV, '/', it.previousPos(), it.currentPos());
//
//            case '=':
//                it.nextChar();
//                //判断下一个字符是不是‘=’
//                startPos=it.previousPos();
//                if(it.peekChar()=='='){
//                    it.nextChar();
//                    return new Token(TokenType.EQ,"==",startPos,it.currentPos());
//                }
//                return new Token(TokenType.ASSIGN, '/', it.previousPos(), it.currentPos());
//
//            case '!':
//                it.nextChar();
//                //不等于判断下一个字符是不是‘=’
//                startPos=it.previousPos();
//                if(it.peekChar()=='='){
//                    it.nextChar();
//                    return new Token(TokenType.NEQ,"!=",startPos,it.currentPos());
//                }
//                else{
//                    //下一个字符不是‘=’抛出异常
//                    throw new TokenizeError(ErrorCode.InvalidInput,it.previousPos());
//                }
//
//            case '<':
//                it.nextChar();
//                //判断下一个字符是不是‘=’
//                startPos=it.previousPos();
//                if(it.peekChar()=='='){
//                    it.nextChar();
//                    return new Token(TokenType.LE,"<=",startPos,it.currentPos());
//                }
//                return new Token(TokenType.LT, '<', it.previousPos(), it.currentPos());
//
//            case '>':
//                it.nextChar();
//                //判断下一个字符是不是‘=’
//                startPos=it.previousPos();
//                if(it.peekChar()=='='){
//                    it.nextChar();
//                    return new Token(TokenType.GE,">=",startPos,it.currentPos());
//                }
//                return new Token(TokenType.GT, '>', it.previousPos(), it.currentPos());
//
//            case '(':
//                it.nextChar();
//                return new Token(TokenType.L_PAREN, '(', it.previousPos(), it.currentPos());
//
//            case ')':
//                it.nextChar();
//                return new Token(TokenType.R_PAREN, ')', it.previousPos(), it.currentPos());
//
//            case '{':
//                it.nextChar();
//                return new Token(TokenType.L_BRACE, '{', it.previousPos(), it.currentPos());
//
//            case '}':
//                it.nextChar();
//                return new Token(TokenType.R_BRACE, '}', it.previousPos(), it.currentPos());
//
//            case ',':
//                it.nextChar();
//                return new Token(TokenType.COMMA, ',', it.previousPos(), it.currentPos());
//
//            case ';':
//                it.nextChar();
//                return new Token(TokenType.SEMICOLON, ';', it.previousPos(), it.currentPos());
//
//            case ':':
//                it.nextChar();
//                return new Token(TokenType.COLON,':',it.previousPos(),it.currentPos());
//            default:
//                it.nextChar();
//                // 不认识这个输入，摸了
//                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
//        }
//    }
//
//    //注释
//    private Token lexComment() throws TokenizeError{
//        it.nextChar();
//        if(it.peekChar()=='/'){
//            while (it.peekChar()!='\n'){
//                it.nextChar();
//            }
//            return null;
//        }
//        else{
//            throw new TokenizeError(ErrorCode.InvalidInput,it.nextPos());
//        }
//    }
//    private void skipSpaceCharacters() {
//        while (!it.isEOF() && Character.isWhitespace(it.peekChar())) {
//            it.nextChar();
//        }
//    }
//}

///////////////////////////////////////////////////////////////////////////////////////
package C0.tokenizer;

import C0.error.ErrorCode;
import C0.error.TokenizeError;
import C0.util.Pos;

//public class Tokenizer {
//
//    private StringIter it;
//
//    public Tokenizer(StringIter it) {
//        this.it = it;
//    }
//    // 这里本来是想实现 Iterator<Token> 的，但是 Iterator 不允许抛异常，于是就这样了
//    /**
//     * 获取下一个 Token
//     *
//     * @return
//     * @throws TokenizeError 如果解析有异常则抛出
//     */
//    public Token nextToken() throws TokenizeError {
//        it.readAll();
//
//        // 跳过之前的所有空白字符
//        skipSpaceCharacters();
//
//        if (it.isEOF()) {
//            return new Token(TokenType.EOF, "", it.currentPos(), it.currentPos());
//        }
//
//        char peek = it.peekChar();
//        /* 数字开头 */
//        if (Character.isDigit(peek)) {
//            return lexUInt();
//        } else if (Character.isAlphabetic(peek)||peek == '_') {
//            return lexIdentOrKeyword();
//        } else if(peek == '"') {
//            return lexStringLiteral();
//        }
////        } else if(peek == '/'){
////            return lexComment();
////        }
//        else {
//            return lexOperatorOrUnknown();
//        }
//    }
//
//
//
//    /* 无符号整数(或浮点数形式) */
//    private Token lexUInt() throws TokenizeError {
//        StringBuilder CatToken = new StringBuilder();
//        Pos startPos = it.nextPos();
//        Pos endPos;
//        int flag = 0;
//        while (!it.isEOF() && (Character.isDigit(it.peekChar()))) {
//            CatToken.append(it.peekChar());
//            it.nextChar();
//        }
//        /*发现小数点，为浮点数 */
//        if (it.peekChar() == '.') {
//            flag = 1;
//            CatToken.append(it.peekChar());
//            it.nextChar();
//            /* 小数点后必须跟数字 */
//            if(Character.isDigit(it.peekChar())){
//                while (!it.isEOF() && (Character.isDigit(it.peekChar()))) {
//                    CatToken.append(it.peekChar());
//                    it.nextChar();
//                }
//                /* 发现e或E，为科学计数法 */
//                if (it.peekChar() == 'e' || it.peekChar() == 'E') {
//                    CatToken.append(it.peekChar());
//                    it.nextChar();
//                    /* 可能有+或- */
//                    if(it.peekChar() == '+' || it.peekChar() == '-'){
//                        CatToken.append(it.peekChar());
//                        it.nextChar();
//                    }
//                    /* 后必须跟数字 */
//                    if(Character.isDigit(it.peekChar())) {
//                        while (!it.isEOF() && (Character.isDigit(it.peekChar()))) {
//                            CatToken.append(it.peekChar());
//                            it.nextChar();
//                        }
//                    }else{
//                        throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
//                    }
//                }
//            }else{
//                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
//            }
//        }
//        endPos = it.currentPos();
//        if (flag == 0) {
//            int num = Integer.parseInt(CatToken.toString());
//            return new Token(TokenType.UINT_LITERAL, num, startPos, endPos);
//        } else {
//            return new Token(TokenType.DOUBLE_LITERAL, Double.parseDouble(CatToken.toString()), startPos, endPos);
//        }
//    }
//
//    /* 关键字或标识符 */
//    private Token lexIdentOrKeyword() throws TokenizeError {
//        // 请填空：
//        // 直到查看下一个字符不是数字或字母为止:
//        // -- 前进一个字符，并存储这个字符
//        //
//        // 尝试将存储的字符串解释为关键字
//        // -- 如果是关键字，则返回关键字类型的 token
//        // -- 否则，返回标识符
//        //
//        // Token 的 Value 应填写标识符或关键字的字符串
//        StringBuilder CatToken=new StringBuilder();
//        Pos startPos=it.nextPos();
//        Pos endPos;
//        while(!it.isEOF() && (Character.isDigit(it.peekChar())||Character.isAlphabetic(it.peekChar())||it.peekChar()=='_')){
//            CatToken.append(it.peekChar());
//            it.nextChar();
//        }
//        endPos=it.currentPos();
//        TokenType[] tokenTypes=TokenType.values();
//        TokenType x = null;
//        for(int i=1;i<=10;i++){
//            if(tokenTypes[i].toString().equalsIgnoreCase(CatToken.toString())){
//                x=tokenTypes[i];
//            }
//        }
//        if(x==null){
//            return new Token(TokenType.IDENT,CatToken.toString(),startPos,endPos);
//        }else {
//            return new Token(x, x.toString(), startPos, endPos);
//        }
//    }
//
//    /* 字符串常量 */
//    private Token lexStringLiteral() throws TokenizeError{
//        StringBuilder CatToken = new StringBuilder();
//        Pos startPos=it.nextPos();
//        Pos endPos;
//        /* 已经判断第一个 " ，将 " 放进字符串中 */
//        CatToken.append(it.peekChar());
//        /* 指向 " 所在的位置，然后循环开始 */
//        it.nextChar();
//        while(!it.isEOF()){
//            /* 判断下一个是否为转义字符 */
//            if(it.peekChar()=='\\'){
//                /* 将'\\'存入，并指向它 */
//                CatToken.append(it.peekChar());
//                it.nextChar();
//                /* 偷看'\\'后的一个字符 */
//                char tmp= it.peekChar();
//                /* 不为转义字符，抛出异常，为转义字符，读取后跳过本次循环*/
//                if(tmp != '\\' && tmp != '\'' && tmp != '"' && tmp != 'n' && tmp != 'r' && tmp != 't'){
//                    throw new TokenizeError(ErrorCode.InvalidInput, it.nextPos());
//                }else{
//                    CatToken.append(it.peekChar());
//                    it.nextChar();
//                    continue;
//                }
//            }
//            /* 判断字符串是否结束,由于上个if已经包含转义字符情况，因此此时一定为函数结束 */
//            if(it.peekChar()=='"'){
//                CatToken.append(it.peekChar());
//                it.nextChar();
//                break;
//            }
//            CatToken.append(it.peekChar());
//            it.nextChar();
//        }
//        endPos=it.currentPos();
//        return new Token(TokenType.STRING_LITERAL,CatToken.toString(),startPos,endPos);
//    }
//
//    /* 计算符 */
//    private Token lexOperatorOrUnknown() throws TokenizeError {
//        /* 首先只用peek对下一个进行读取，判断成功后再next */
//        Pos startpos;
//        switch (it.peekChar()) {
//            case '+':
//                it.nextChar();
//                return new Token(TokenType.PLUS, '+', it.previousPos(), it.currentPos());
//
//            case '-':
//                // 填入返回语句
//                it.nextChar();
//                startpos=it.previousPos();
//                if(it.peekChar()=='>'){
//                    it.nextChar();
//                    return new Token(TokenType.ARROW, "->", startpos, it.currentPos());
//                }
//                return new Token(TokenType.MINUS, '-', it.previousPos(), it.currentPos());
//
//            case '*':
//                // 填入返回语句
//                it.nextChar();
//                return new Token(TokenType.MUL, '*', it.previousPos(), it.currentPos());
//
//            case '/':
//                // 填入返回语句
//                it.nextChar();
//                return new Token(TokenType.DIV, '/', it.previousPos(), it.currentPos());
//
//            case '=':
//                // 填入返回语句
//                it.nextChar();
//                startpos=it.previousPos();
//                if(it.peekChar()=='='){
//                    it.nextChar();
//                    return new Token(TokenType.EQ, "==", startpos, it.currentPos());
//                }
//                return new Token(TokenType.ASSIGN, '=', it.previousPos(), it.currentPos());
//
//            case '!':
//                // 填入返回语句
//                it.nextChar();
//                startpos=it.previousPos();
//                if(it.peekChar()=='='){
//                    it.nextChar();
//                    return new Token(TokenType.NEQ, "!=", startpos, it.currentPos());
//                }else {
//                    throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
//                }
//
//            case '<':
//                // 填入返回语句
//                it.nextChar();
//                startpos=it.previousPos();
//                if(it.peekChar()=='='){
//                    it.nextChar();
//                    return new Token(TokenType.LE, "<=", startpos, it.currentPos());
//                }
//                return new Token(TokenType.LT, '<', it.previousPos(), it.currentPos());
//
//            case '>':
//                // 填入返回语句
//                it.nextChar();
//                startpos=it.previousPos();
//                if(it.peekChar()=='='){
//                    it.nextChar();
//                    return new Token(TokenType.GE, ">=", startpos, it.currentPos());
//                }
//                return new Token(TokenType.GT, '>', it.previousPos(), it.currentPos());
//
//            case '(':
//                // 填入返回语句
//                it.nextChar();
//                return new Token(TokenType.L_PAREN, '(', it.previousPos(), it.currentPos());
//
//            case ')':
//                // 填入返回语句
//                it.nextChar();
//                return new Token(TokenType.R_PAREN, ')', it.previousPos(), it.currentPos());
//
//            case '{':
//                // 填入返回语句
//                it.nextChar();
//                return new Token(TokenType.L_BRACE, '{', it.previousPos(), it.currentPos());
//
//            case '}':
//                // 填入返回语句
//                it.nextChar();
//                return new Token(TokenType.R_BRACE, '}', it.previousPos(), it.currentPos());
//
//            case ',':
//                // 填入返回语句
//                it.nextChar();
//                return new Token(TokenType.COMMA, ',', it.previousPos(), it.currentPos());
//
//            case ':':
//                // 填入返回语句
//                it.nextChar();
//                return new Token(TokenType.COLON, ':', it.previousPos(), it.currentPos());
//            case ';':
//                // 填入返回语句
//                it.nextChar();
//                return new Token(TokenType.SEMICOLON, ';', it.previousPos(), it.currentPos());
//
//            // 填入更多状态和返回语句
//
//            default:
//                // 不认识这个输入，摸了
//                it.nextChar();
//                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
//        }
//    }
//
//    /* 注释 */
//    private Token lexComment() throws TokenizeError{
//        it.nextChar();
//        if(it.peekChar()=='/'){
//            while(it.peekChar()!='\n'){
//                it.nextChar();
//            }
//            return null;
//        }else{
//            throw new TokenizeError(ErrorCode.InvalidInput, it.nextPos());
//        }
//    }
//
//    private void skipSpaceCharacters() {
//        while (!it.isEOF() && Character.isWhitespace(it.peekChar())) {
//            it.nextChar();
//        }
//    }
//}

//public class Tokenizer {
//
//    private StringIter it;
//
//    public Tokenizer(StringIter it) {
//        this.it = it;
//    }
//
//    // 这里本来是想实现 Iterator<Token> 的，但是 Iterator 不允许抛异常，于是就这样了
//    /**
//     * 获取下一个 Token
//     *
//     * @return
//     * @throws TokenizeError 如果解析有异常则抛出
//     */
//    public Token nextToken() throws TokenizeError {
//        it.readAll();
//
//        // 跳过之前的所有空白字符
//        skipSpaceCharacters();
//
//        if (it.isEOF()) {
//            return new Token(TokenType.EOF, "", it.currentPos(), it.currentPos());
//        }
//
//        char peek = it.peekChar();
//        if (Character.isDigit(peek)) {
//            return lexUInt();
//        } else if (Character.isAlphabetic(peek)||peek=='_') {
//            return lexIdentOrKeyword();
//        } else if (peek=='\"'){
//            return lexString();
//        } else {
//            Token token=lexOperatorOrUnknown();
//            if (token==null) return nextToken();
//            return token;
//        }
//    }
//    private Token lexString() throws  TokenizeError{
//        Pos begin=it.currentPos();
//        it.nextChar();
//        char ch;
//        String temp=new String();
//        while ((ch=it.peekChar())!='"'){
//            if (it.isEOF()){
//                throw new TokenizeError(ErrorCode.InvalidInput, begin);
//            }
//            if (ch=='\\'){
//                it.nextChar();
//                if ((ch=it.peekChar())=='\\'){
//                    temp+='\\';
//                }
//                else if (ch=='\'') temp+='\'';
//                else if (ch == '\"') temp += '\"';
//                else if (ch == 'n') temp += '\n';
//                else if (ch == 't') temp += '\t';
//                else if (ch == 'r') temp += '\r';
//                else throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
//            }
//            else {
//                temp+=ch;
//            }
//            it.nextChar();
//        }
//        it.nextChar();
//        return new Token(TokenType.STRING_LITERAL,temp,begin,it.currentPos());
//    }
//    private Token lexUInt() throws TokenizeError {
//        // 请填空：
//        // 直到查看下一个字符不是数字为止:
//        Pos begin=it.currentPos();
//        String temp=new String();
//        temp+=it.nextChar();
//        while(Character.isDigit(it.peekChar())){
//            temp+=it.nextChar();
//        }
//        long a=0;
//        a=Long.parseLong(temp);
//
//        return new Token(TokenType.UINT_LITERAL, a, begin, it.currentPos());
//        // -- 前进一个字符，并存储这个字符
//        //
//        // 解析存储的字符串为无符号整数
//        // 解析成功则返回无符号整数类型的token，否则返回编译错误
//        //
//        // Token 的 Value 应填写数字的值
//
//    }
//
//    private Token lexIdentOrKeyword() throws TokenizeError {
//        // 请填空：
//        Pos begin=it.currentPos();
//        String temp=new String();
//        boolean is_ident=false;
//        if (it.peekChar()=='_') is_ident=true;
//        temp+=it.nextChar();
//        if (Character.isDigit(it.peekChar())&&is_ident){
//            throw new TokenizeError(ErrorCode.InvalidIdentifier,begin);
//        }
//        while (true){
//            if (Character.isLetter(it.peekChar())){
//                temp+=it.nextChar();
//            }
//            else if (Character.isDigit(it.peekChar())||it.peekChar()=='_'){
//                is_ident=true;
//                temp+=it.nextChar();
//            }
//            else break;
//        }
//
//        if (is_ident){
//            return new Token(TokenType.IDENT,temp,begin,it.currentPos());
//        }
//        switch (temp){
//            case "fn":
//                return new Token(TokenType.FN_KW,temp,begin,it.currentPos());
//            case "let":
//                return new Token(TokenType.LET_KW,temp,begin,it.currentPos());
//            case "const" :
//                return new Token(TokenType.CONST_KW,temp,begin,it.currentPos());
//            case "as" :
//                return new Token(TokenType.AS_KW,temp,begin,it.currentPos());
//            case "while" :
//                return new Token(TokenType.WHILE_KW,temp,begin,it.currentPos());
//            case "if" :
//                return new Token(TokenType.IF_KW,temp,begin,it.currentPos());
//            case "else" :
//                return new Token(TokenType.ELSE_KW,temp,begin,it.currentPos());
//            case "break":
//                return new Token(TokenType.BREAK_KW,temp,begin,it.currentPos());
//            case "return" :
//                return new Token(TokenType.RETURN_KW,temp,begin,it.currentPos());
//            case "continue":
//                return new Token(TokenType.CONTINUE_KW,temp,begin,it.currentPos());
//            default:
//                return new Token(TokenType.IDENT,temp,begin,it.currentPos());
//        }
//        // 直到查看下一个字符不是数字或字母为止:
//        // -- 前进一个字符，并存储这个字符
//        //
//        // 尝试将存储的字符串解释为关键字
//        // -- 如果是关键字，则返回关键字类型的 token
//        // -- 否则，返回标识符
//        //
//        // Token 的 Value 应填写标识符或关键字的字符串
//    }
//
//    private Token lexOperatorOrUnknown() throws TokenizeError {
//        switch (it.nextChar()) {
//            case '+':
//                return new Token(TokenType.PLUS, "+", it.previousPos(), it.currentPos());
//            case '-':
//                if (it.peekChar() == '>') {
//                    it.nextChar();
//                    return new Token(TokenType.ARROW, "->", it.previousPos(), it.currentPos());
//                } else {
//                    return new Token(TokenType.MINUS, "-", it.previousPos(), it.currentPos());
//                }
//            case '*':
//                return new Token(TokenType.MUL, "*", it.previousPos(), it.currentPos());
//            case '=':
//                if (it.peekChar() == '=') {
//                    it.nextChar();
//                    return new Token(TokenType.EQ, "==", it.previousPos(), it.currentPos());
//                } else {
//                    return new Token(TokenType.ASSIGN, "=", it.previousPos(), it.currentPos());
//                }
//            case '/':
//                if (it.peekChar() == '/') {
//                    skipComment();
//                    return null;
//                } else {
//                    return new Token(TokenType.DIV, "/", it.previousPos(), it.currentPos());
//                }
//            case '!':
//                if (it.peekChar() == '=') {
//                    it.nextChar();
//                    return new Token(TokenType.NEQ, "!=", it.previousPos(), it.currentPos());
//                } else {
//                    throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
//                }
//            case '<':
//                if (it.peekChar() == '=') {
//                    it.nextChar();
//                    return new Token(TokenType.LE, "<=", it.previousPos(), it.currentPos());
//                } else {
//                    return new Token(TokenType.LT, "<", it.previousPos(), it.currentPos());
//                }
//            case '>':
//                if (it.peekChar() == '=') {
//                    it.nextChar();
//                    return new Token(TokenType.GE, ">=", it.previousPos(), it.currentPos());
//                } else {
//                    return new Token(TokenType.GT, ">", it.previousPos(), it.currentPos());
//                }
//            case '(':
//                return new Token(TokenType.L_PAREN, "(", it.previousPos(), it.currentPos());
//            case ')':
//                return new Token(TokenType.R_PAREN, ")", it.previousPos(), it.currentPos());
//            case '{':
//                return new Token(TokenType.L_BRACE, "{", it.previousPos(), it.currentPos());
//            case '}':
//                return new Token(TokenType.R_BRACE, "}", it.previousPos(), it.currentPos());
//            case ',':
//                return new Token(TokenType.COMMA, ",", it.previousPos(), it.currentPos());
//            case ':':
//                return new Token(TokenType.COLON, ":", it.previousPos(), it.currentPos());
//            case ';':
//                return new Token(TokenType.SEMICOLON, ";", it.previousPos(), it.currentPos());
//            default:
//                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
//        }
//    }
//
//    private void skipSpaceCharacters() {
//        while (!it.isEOF() && Character.isWhitespace(it.peekChar())) {
//            it.nextChar();
//        }
//    }
//
//    private void skipComment() {
//        it.nextChar();
//        while (it.peekChar() != '\n') it.nextChar();
//    }
//}

public class Tokenizer {

    private StringIter it;

    public Tokenizer(StringIter it) {
        this.it = it;
    }

    // 这里本来是想实现 Iterator<Token> 的，但是 Iterator 不允许抛异常，于是就这样了

    public StringIter getIt() {
        return it;
    }

    /**
     * 获取下一个 Token
     *
     * @return
     * @throws TokenizeError 如果解析有异常则抛出
     */
    public Token nextToken() throws TokenizeError {
        Token re;
        it.readAll();

        // 跳过之前的所有空白字符
        skipSpaceCharacters();
        /** 单独考虑注释 直接过滤掉*/
        while(it.peekChar()=='/') {
            it.nextChar();
            if (it.peekChar() == '/') {
                it.nextChar();
                skipComment();
            } else {
                return new Token(TokenType.DIV, '/', it.previousPos(), it.currentPos());
            }
            skipSpaceCharacters();
        }

        if (it.isEOF()) {
            return new Token(TokenType.EOF, "", it.currentPos(), it.currentPos());
        }

        char peek = it.peekChar();
        if (Character.isDigit(peek)) {
            re =  lexUInt();
        } else if (Character.isAlphabetic(peek)||peek=='_') {
            re = lexIdentOrKeyword();
        }else if (peek=='"'){
            re= lexString();
        }
        else {
            re= lexOperatorOrUnknown();
        }
//        System.out.print(re.getValueString()+" ");
        return  re;
    }

    private Token lexUInt() throws TokenizeError {
        // 请填空：
        // 直到查看下一个字符不是数字为止:
        StringBuilder number = new StringBuilder();
        Pos startpos = it.currentPos();
        number.append(it.nextChar());
        while (Character.isDigit(it.peekChar())) {
            // -- 前进一个字符，并存储这个字符
            number.append(it.nextChar());
        }
        //
        // 解析存储的字符串为无符号整数
        // 解析成功则返回无符号整数类型的token，否则返回编译错误
        //
        // Token 的 Value 应填写数字的值
        try {
            Integer num = Integer.valueOf(number.toString());
            return new Token(TokenType.UINT_LITERAL, num, startpos, it.currentPos());
        } catch (Exception e) {
            throw new TokenizeError(ErrorCode.InvalidInput, startpos);
        }
    }

    private Token lexIdentOrKeyword() throws TokenizeError {
        // 请填空：
        // 直到查看下一个字符不是数字或字母或_为止:
        StringBuilder temp_token=new StringBuilder();
        Pos startpos=it.currentPos();
        do{
            // -- 前进一个字符，并存储这个字符
            temp_token.append(it.nextChar());
        }while(Character.isAlphabetic(it.peekChar())||Character.isDigit(it.peekChar())||it.peekChar()=='_');
        TokenType type;
        String token=temp_token.toString();
        try {
            switch (token) {
                case "fn": {
                    type = TokenType.FN_KW;
                    break;
                }
                case "let": {
                    type = TokenType.LET_KW;
                    break;
                }
                case "const": {
                    type = TokenType.CONST_KW;
                    break;
                }
                case "as": {
                    type = TokenType.AS_KW;
                    break;
                }
                case "while": {
                    type = TokenType.WHILE_KW;
                    break;
                }
                case "else": {
                    type = TokenType.ELSE_KW;
                    break;
                }
                case "if": {
                    type = TokenType.IF_KW;
                    break;
                }
                case "return": {
                    type = TokenType.RETURN_KW;
                    break;
                }
                default:
                    type = TokenType.IDENT;
            }
            //
            // Token 的 Value 应填写标识符或关键字的字符串
            return new Token(type,token,startpos,it.currentPos());
        }catch (Exception e) {
            throw new TokenizeError(ErrorCode.InvalidInput, startpos);
        }
    }

    private Token lexString() throws TokenizeError {
        StringBuilder str = new StringBuilder();
        Pos startpos = it.currentPos();
//        str.append(it.nextChar());
        it.nextChar();
        while (it.peekChar() != '"') {
            if (it.peekChar() == '\\') { //转义字符
                it.nextChar();

                if (it.peekChar() == '\\'){
                    str.append('\\');
                }
                else if (it.peekChar() == '\''){
                    str.append('\'');
                }
                else if (it.peekChar() == '\"'){
                    str.append('\"');
                }
                else if (it.peekChar() == 'n'){
                    str.append('\n');
                }
                else if (it.peekChar() == 't'){
                    str.append('\t');
                }
                else if (it.peekChar() == 'r'){
                    str.append('\r');
                }
                else
                    throw new TokenizeError(ErrorCode.InvalidInput, startpos);
                it.nextChar();
            }
            else {
                str.append(it.nextChar());
            }
        }
        it.nextChar();
//        str.append(it.nextChar());
        return new Token(TokenType.STRING_LITERAL, str.toString(), startpos, it.currentPos());
    }

    private Token lexOperatorOrUnknown() throws TokenizeError {
        switch (it.nextChar()) {
            case '+':
                return new Token(TokenType.PLUS, '+', it.previousPos(), it.currentPos());

            case '-':
                // 填入返回语句
                if(it.peekChar()=='>'){
                    it.nextChar();
                    return new Token(TokenType.ARROW, "->", it.previousPos(), it.currentPos());
                }
                return new Token(TokenType.MINUS, '-', it.previousPos(), it.currentPos());

            case '*':
                // 填入返回语句
                return new Token(TokenType.MUL, '*', it.previousPos(), it.currentPos());

            case '/':
                // 填入返回语句
                return new Token(TokenType.DIV, '/', it.previousPos(), it.currentPos());
            // 填入更多状态和返回语句
            case '=':
                // 填入返回语句
                if(it.peekChar()=='=') {
                    it.nextChar();
                    return new Token(TokenType.EQ, "==", it.previousPos(), it.currentPos());
                }
                return new Token(TokenType.ASSIGN, '=', it.previousPos(), it.currentPos());
            case '!':
                // 填入返回语句
                if(it.peekChar()=='=') {
                    it.nextChar();
                    return new Token(TokenType.NEQ, "!=", it.previousPos(), it.currentPos());
                }
                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
            case '<':
                // 填入返回语句
                if(it.peekChar()=='=') {
                    it.nextChar();
                    return new Token(TokenType.LE, "<=", it.previousPos(), it.currentPos());
                }
                return new Token(TokenType.LT, '<', it.previousPos(), it.currentPos());
            case '>':
                // 填入返回语句
                if(it.peekChar()=='=') {
                    it.nextChar();
                    return new Token(TokenType.GE, ">=", it.previousPos(), it.currentPos());
                }
                return new Token(TokenType.GT, '>', it.previousPos(), it.currentPos());
            case '(':
                // 填入返回语句
                return new Token(TokenType.L_PAREN, '(', it.previousPos(), it.currentPos());
            case ')':
                // 填入返回语句
                return new Token(TokenType.R_PAREN, ')', it.previousPos(), it.currentPos());
            case '{':
                // 填入返回语句
                return new Token(TokenType.L_BRACE, '{', it.previousPos(), it.currentPos());
            case '}':
                // 填入返回语句
                return new Token(TokenType.R_BRACE, '}', it.previousPos(), it.currentPos());
            case ',':
                // 填入返回语句
                return new Token(TokenType.COMMA, ',', it.previousPos(), it.currentPos());
            case ':':
                // 填入返回语句
                return new Token(TokenType.COLON, ':', it.previousPos(), it.currentPos());
            case ';':
                // 填入返回语句
                return new Token(TokenType.SEMICOLON, ';', it.previousPos(), it.currentPos());

            default:
                // 不认识这个输入，摸了
                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
        }
    }

    private void skipSpaceCharacters() {
        while (!it.isEOF() && Character.isWhitespace(it.peekChar())) {
            it.nextChar();
        }
    }

    private void skipComment() {
        while (!it.isEOF() && it.peekChar()!='\n') {
            it.nextChar();
        }
    }
}
