package C0.tokenizer;

import C0.error.ErrorCode;
import C0.error.TokenizeError;
import C0.util.Pos;

public class Tokenizer {

    private StringIter it;

    public Tokenizer(StringIter it) {
        this.it = it;
    }

    // 这里本来是想实现 Iterator<Token> 的，但是 Iterator 不允许抛异常，于是就这样了
    /**
     * 获取下一个 Token
     * 
     * @return
     * @throws TokenizeError 如果解析有异常则抛出
     */
    public Token nextToken() throws TokenizeError {
        it.readAll();

        // 跳过之前的所有空白字符
        skipSpaceCharacters();

        if (it.isEOF()) {
            return new Token(TokenType.EOF, "", it.currentPos(), it.currentPos());
        }

        char peek = it.peekChar();
        if (Character.isDigit(peek)) {
            return lexUInt();
        } else if (Character.isAlphabetic(peek)||peek=='_') {
            return lexIdentOrKeyword();
        } else if(peek=='"'){
            return lexStringLiteral();
        }else {
            return lexOperatorOrUnknown();
        }
    }

    //无符号整数
    private Token lexUInt() throws TokenizeError {
        Pos startPos=it.currentPos();
        StringBuilder digitStr=new StringBuilder();
        while(!it.isEOF()&&Character.isDigit(it.peekChar())){
            digitStr.append(it.nextChar());
        }
        try{
            int digit=Integer.parseInt(digitStr.toString());
            return new Token(TokenType.UINT_LITERAL,digit,startPos,it.currentPos());
        }catch (Exception e){
            throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
        }
    }

    //关键字或者标识符
    private Token lexIdentOrKeyword() throws TokenizeError {
        Pos startPos=it.currentPos();
        StringBuilder identStr=new StringBuilder();
        while(!it.isEOF()&&(Character.isDigit(it.peekChar())||Character.isLetter(it.peekChar())||it.peekChar()=='_')){
            identStr.append(it.nextChar());
        }
        String ident=identStr.toString();
        TokenType tokenType;
        switch (ident){
            case "fn":
                tokenType=TokenType.FN_KW;
                break;
            case "let":
                tokenType=TokenType.LET_KW;
                break;
            case "const":
                tokenType=TokenType.CONST_KW;
                break;
            case "as":
                tokenType=TokenType.AS_KW;
                break;
            case "while":
                tokenType=TokenType.WHILE_KW;
                break;
            case "if":
                tokenType=TokenType.IF_KW;
                break;
            case "else":
                tokenType=TokenType.ELSE_KW;
                break;
            case "return":
                tokenType=TokenType.RETURN_KW;
                break;
            case "break":
                tokenType=TokenType.BREAK_KW;
                break;
            case "continue":
                tokenType=TokenType.CONTINUE_KW;
                break;
            default:
                tokenType=TokenType.IDENT;
                break;
        }
        return new Token(tokenType,ident,startPos,it.currentPos());
    }

    //字符串常量
    private Token lexStringLiteral() throws TokenizeError{
        Pos startPos=it.currentPos();
        StringBuilder strLiteral=new StringBuilder();
        //将入口处判断的"加入字符串常量
        strLiteral.append(it.nextChar());
        while(!it.isEOF()){
            //判断是否是字符串常量结尾
            if(it.peekChar()=='"'){
                strLiteral.append(it.nextChar());
                break;
            }
            if(it.peekChar()=='\\'){
                it.nextChar();
                char peek=it.peekChar();
                if(peek=='\\'||peek=='\''||peek=='\"'||peek=='\n'||peek=='\t'||peek=='\r'){
                    strLiteral.append(it.nextChar());
                }
                else{
                    throw new TokenizeError(ErrorCode.InvalidInput,it.nextPos());
                }
            }
            else {
                strLiteral.append(it.nextChar());
            }
        }
        return new Token(TokenType.STRING_LITERAL,strLiteral.toString(),startPos,it.currentPos());
    }

    //运算符
    private Token lexOperatorOrUnknown() throws TokenizeError {
        Pos startPos;
        switch (it.nextChar()) {
            case '+':
                return new Token(TokenType.PLUS, '+', it.previousPos(), it.currentPos());

            case '-':
                //读到“-”判断是“-”还是“->”
                startPos=it.previousPos();
                if(it.peekChar()=='>'){
                    it.nextChar();
                    return new Token(TokenType.ARROW,"->",startPos,it.currentPos());
                }
                return new Token(TokenType.MINUS, '-', it.previousPos(), it.currentPos());

            case '*':
                return new Token(TokenType.MUL, '*', it.previousPos(), it.currentPos());

            case '/':
                return new Token(TokenType.DIV, '/', it.previousPos(), it.currentPos());

            case '=':
                //判断下一个字符是不是‘=’
                startPos=it.previousPos();
                if(it.peekChar()=='='){
                    it.nextChar();
                    return new Token(TokenType.EQ,"==",startPos,it.currentPos());
                }
                return new Token(TokenType.ASSIGN, '/', it.previousPos(), it.currentPos());

            case '!':
                //不等于判断下一个字符是不是‘=’
                startPos=it.previousPos();
                if(it.peekChar()=='='){
                    it.nextChar();
                    return new Token(TokenType.NEQ,"!=",startPos,it.currentPos());
                }
                else{
                    //下一个字符不是‘=’抛出异常
                    throw new TokenizeError(ErrorCode.InvalidInput,it.previousPos());
                }

            case '<':
                //判断下一个字符是不是‘=’
                startPos=it.previousPos();
                if(it.peekChar()=='='){
                    it.nextChar();
                    return new Token(TokenType.LE,"<=",startPos,it.currentPos());
                }
                return new Token(TokenType.LT, '<', it.previousPos(), it.currentPos());

            case '>':
                //判断下一个字符是不是‘=’
                startPos=it.previousPos();
                if(it.peekChar()=='='){
                    it.nextChar();
                    return new Token(TokenType.GE,">=",startPos,it.currentPos());
                }
                return new Token(TokenType.GT, '>', it.previousPos(), it.currentPos());

            case '(':
                return new Token(TokenType.L_PAREN, '(', it.previousPos(), it.currentPos());

            case ')':
                return new Token(TokenType.R_PAREN, ')', it.previousPos(), it.currentPos());

            case '{':
                return new Token(TokenType.L_BRACE, '{', it.previousPos(), it.currentPos());

            case '}':
                return new Token(TokenType.R_BRACE, '}', it.previousPos(), it.currentPos());

            case ',':
                return new Token(TokenType.COMMA, ',', it.previousPos(), it.currentPos());

            case ';':
                return new Token(TokenType.SEMICOLON, ';', it.previousPos(), it.currentPos());

            case ':':
                return new Token(TokenType.COLON,':',it.previousPos(),it.currentPos());
            default:
                // 不认识这个输入，摸了
                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
        }
    }

    //注释
    private Token lexComment() throws TokenizeError{
        it.nextChar();
        if(it.peekChar()=='/'){
            while (it.peekChar()!='\n'){
                it.nextChar();
            }
            return null;
        }
        else{
            throw new TokenizeError(ErrorCode.InvalidInput,it.nextPos());
        }
    }
    private void skipSpaceCharacters() {
        while (!it.isEOF() && Character.isWhitespace(it.peekChar())) {
            it.nextChar();
        }
    }
}
