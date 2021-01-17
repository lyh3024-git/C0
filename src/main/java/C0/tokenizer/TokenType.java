package C0.tokenizer;

public enum TokenType {
    /** 空 */
    None,

    //关键字
    /** fn */
    FN_KW,
    /** let */
    LET_KW,
    /** const */
    CONST_KW,
    /** as */
    AS_KW,
    /** while */
    WHILE_KW,
    /** if */
    IF_KW,
    /** else */
    ELSE_KW,
    /** return */
    RETURN_KW,
    /** break */
    BREAK_KW,
    /** continue */
    CONTINUE_KW,

    //字面量
    /** 无符号整数 */
    UINT_LITERAL,
    /** 字符串常量 */
    STRING_LITERAL,
    /** 浮点数常量 */
    DOUBLE_LITERAL,
    /** 字符常量 */
    CHAR_LITERAL,

    /**标识符*/
    IDENT,

    //运算符
    /** 加号 */
    PLUS,
    /** 减号 */
    MINUS,
    /** 乘号 */
    MUL,
    /** 除号 */
    DIV,
    /** 等号 */
    ASSIGN,
    /**等等号*/
    EQ,
    /**不等号*/
    NEQ,
    /**小于号*/
    LT,
    /**大于号*/
    GT,
    /**小于等于*/
    LE,
    /**大于等于*/
    GE,
    /**左括号*/
    L_PAREN,
    /**右括号*/
    R_PAREN,
    /**左中括号*/
    L_BRACE,
    /**右中括号*/
    R_BRACE,
    /**箭头符号*/
    ARROW,
    /**逗号*/
    COMMA,
    /**冒号*/
    COLON,
    /**分号*/
    SEMICOLON,

    /** 文件尾 */
    EOF;

    @Override
    public String toString() {
        switch (this) {
            case None:
                return "NullToken";

            //关键字
            case FN_KW:
                return "fn";
            case LET_KW:
                return "let";
            case CONST_KW:
                return "const";
            case AS_KW:
                return "as";
            case WHILE_KW:
                return "while";
            case IF_KW:
                return "if";
            case ELSE_KW:
                return "else";
            case RETURN_KW:
                return "return";
            case BREAK_KW:
                return "break";
            case CONTINUE_KW:
                return "continue";

            //字面量
            case UINT_LITERAL:
                return "UnsignedInteger";
            case STRING_LITERAL:
                return "String";
            case DOUBLE_LITERAL:
                return "Double";
            case CHAR_LITERAL:
                return "Char";

            //标识符
            case IDENT:
                return "Ident";

            //运算符
            case PLUS:
                return "Plus";
            case MINUS:
                return "Minus";
            case MUL:
                return "Mul";
            case DIV:
                return "Div";
            case ASSIGN:
                return "Assign";
            case EQ:
                return "Equal";
            case NEQ:
                return "NotEqual";
            case LT:
                return "LT";
            case GT:
                return "GT";
            case LE:
                return "LE";
            case GE:
                return "GE";
            case L_PAREN:
                return "LParen";
            case R_PAREN:
                return "RParen";
            case L_BRACE:
                return "LBrace";
            case R_BRACE:
                return "RBrace";
            case ARROW:
                return "Arrow";
            case COMMA:
                return "Comma";
            case COLON:
                return "Colon";
            case SEMICOLON:
                return "Semicolon";

            case EOF:
                return "EOF";
            default:
                return "InvalidToken";
        }
    }
}
