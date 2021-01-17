package C0.struct;

import C0.instruction.Instruction;
import C0.util.Type;

import java.util.List;

public class FunctionDef {
    int offset;//函数名称在全局变量中的位置
    int return_slots;//返回值占据的 slot 数
    int param_slots;//参数占据的 slot 数
    int loc_slots;//局部变量占据的 slot 数
    List<Instruction> instructions;//函数体的指令

    String name;//函数名
    List<Parameter> parameters;//参数列表
    Type type;//函数返回值类型void/int
    int functionID;//函数ID

    public FunctionDef(int offset,String name, int functionID) {
        this.offset=offset;
        this.name = name;
        this.return_slots=0;
        this.param_slots=0;
        this.loc_slots=0;
        this.instructions=null;
        this.parameters=null;
        this.type=Type.VOID;
        this.functionID = functionID;
    }

    public FunctionDef(int offset, int return_slots, int param_slots, int loc_slots, List<Instruction> instructions, String name, List<Parameter> parameters, Type type, int functionID) {
        this.offset = offset;
        this.return_slots = return_slots;
        this.param_slots = param_slots;
        this.loc_slots = loc_slots;
        this.instructions = instructions;
        this.name = name;
        this.parameters = parameters;
        this.type = type;
        this.functionID = functionID;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getReturn_slots() {
        return return_slots;
    }

    public void setReturn_slots(int return_slots) {
        this.return_slots = return_slots;
    }

    public int getParam_slots() {
        return param_slots;
    }

    public void setParam_slots(int param_slots) {
        this.param_slots = param_slots;
    }

    public int getLoc_slots() {
        return loc_slots;
    }

    public void setLoc_slots(int loc_slots) {
        this.loc_slots = loc_slots;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getFunctionID() {
        return functionID;
    }

    public void setFunctionID(int functionID) {
        this.functionID = functionID;
    }
}
