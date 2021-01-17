package C0.struct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GlobalDef {
    String name;
    int is_const;//是否为常量？非零值视为真
    char[] byteValues;//按字节顺序排列的变量值

    public GlobalDef(String name, int is_const) {
        this.name = name;
        this.is_const = is_const;
        this.byteValues=null;
    }

    public GlobalDef(String name,int is_const,char[] byteValues){
        this.name=name;
        this.is_const=is_const;
        this.byteValues=byteValues;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIs_const() {
        return is_const;
    }

    public void setIs_const(int is_const) {
        this.is_const = is_const;
    }

    public char[] getByteValues() {
        return byteValues;
    }

    public void setByteValues(char[] byteValues) {
        this.byteValues = byteValues;
    }
}
