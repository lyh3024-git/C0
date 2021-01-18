package C0.analyser;

import C0.tokenizer.TokenType;

public class OperatorPrecedence {
    //       + - * /   ( ) < >   <= >= == !=
    private static int[][] priority={
            {1,1,-1,-1,-1,1,1,1,1,1,1,1},
            {1,1,-1,-1,-1,1,1,1,1,1,1,1},
            {1,1,1,1,-1,1,1,1,1,1,1,1},
            {1,1,1,1,-1,1,1,1,1,1,1,1},
            {-1,-1,-1,-1,-1,2,-1,-1,-1,-1,-1,-1},
            {-1,-1,-1,-1,0,0,-1,-1,-1,-1,-1,-1},
            {-1,-1,-1,-1,-1,1,1,1,1,1,1,1},
            {-1,-1,-1,-1,-1,1,1,1,1,1,1,1},
            {-1,-1,-1,-1,-1,1,1,1,1,1,1,1},
            {-1,-1,-1,-1,-1,1,1,1,1,1,1,1},
            {-1,-1,-1,-1,-1,1,1,1,1,1,1,1},
            {-1,-1,-1,-1,-1,1,1,1,1,1,1,1},
    };

    public static int getOffset(TokenType tokenType){
        switch (tokenType){
            case PLUS:return 0;
            case MINUS:return 1;
            case MUL:return 2;
            case DIV:return 3;
            case L_PAREN:return 4;
            case R_PAREN:return 5;
            case LT:return 6;
            case GT:return 7;
            case LE:return 8;
            case GE:return 9;
            case EQ:return 10;
            case NEQ:return 11;
            default:return -1;
        }
    }

    public static int getPriority(int first,int second){
        return priority[first][second];
    }
}
