package C0.struct;

import C0.util.Type;

public class Symbol {
    boolean isConstant;
    boolean isInitialized;
    int offset;//表示符号表中偏移量

    String name;
    Type type;//表示变量的类型void/int或者常量的类型int/string
    int level;//表示嵌套层次

    public Symbol(boolean isConstant, boolean isInitialized, int offset, String name, Type type, int level) {
        this.isConstant = isConstant;
        this.isInitialized = isInitialized;
        this.offset = offset;
        this.name = name;
        this.type = type;
        this.level = level;
    }


    /**
     * @return the stackOffset
     */
    public int getOffset() {
        return offset;
    }

    /**
     * @return the isConstant
     */
    public boolean isConstant() {
        return isConstant;
    }

    /**
     * @return the isInitialized
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * @param isConstant the isConstant to set
     */
    public void setConstant(boolean isConstant) {
        this.isConstant = isConstant;
    }

    /**
     * @param isInitialized the isInitialized to set
     */
    public void setInitialized(boolean isInitialized) {
        this.isInitialized = isInitialized;
    }

    /**
     * @param offset the offset to set
     */
    public void setoffset(int offset) {
        this.offset = offset;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }
}
