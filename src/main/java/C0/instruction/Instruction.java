package C0.instruction;

import C0.tokenizer.TokenType;

import java.util.List;
import java.util.Objects;

public class Instruction {
    private Operation opt;
    long x;
    int ByteNum;

    public Instruction(Operation opt) {
        this.opt = opt;
        this.x = 0;
        this.ByteNum=0;
    }

    public Instruction(Operation opt, long x,int ByteNum) {
        this.opt = opt;
        this.x = x;
        this.ByteNum=ByteNum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Instruction that = (Instruction) o;
        return opt == that.opt && Objects.equals(x, that.x);
    }

    @Override
    public int hashCode() {
        return Objects.hash(opt, x);
    }

    public Operation getOpt() {
        return opt;
    }

    public void setOpt(Operation opt) {
        this.opt = opt;
    }

    public long getX() {
        return x;
    }

    public void setX(long x) {
        this.x = x;
    }

    public void setByteNum(int ByteNum) {this.ByteNum=ByteNum;}

    public int getByteNum() {return this.ByteNum;}

    public static void addInstruction(TokenType tokenType, List<Instruction> instructionList){
        switch (tokenType){
            case EQ:
                instructionList.add(new Instruction(Operation.cmp));
                instructionList.add(new Instruction(Operation.not));
                break;
            case NEQ:
                instructionList.add(new Instruction(Operation.cmp));
                break;
            case GT:
                instructionList.add(new Instruction(Operation.cmp));
                instructionList.add(new Instruction(Operation.set_gt));
                break;
            case LT:
                instructionList.add(new Instruction(Operation.cmp));
                instructionList.add(new Instruction(Operation.set_lt));
                break;
            case GE:
                instructionList.add(new Instruction(Operation.cmp));
                instructionList.add(new Instruction(Operation.set_lt));
                instructionList.add(new Instruction(Operation.not));
                break;
            case LE:
                instructionList.add(new Instruction(Operation.cmp));
                instructionList.add(new Instruction(Operation.set_gt));
                instructionList.add(new Instruction(Operation.not));
                break;
            case PLUS:
                instructionList.add(new Instruction(Operation.add));
                break;
            case MINUS:
                instructionList.add(new Instruction(Operation.sub));
                break;
            case MUL:
                instructionList.add(new Instruction(Operation.mul));
                break;
            case DIV:
                instructionList.add(new Instruction(Operation.div));
                break;
            default:
                break;
        }
    }
}
