package C0;

import java.awt.desktop.OpenURIEvent;
import java.io.*;
import java.util.*;

import C0.analyser.Analyser;
import C0.error.CompileError;
import C0.instruction.Instruction;
import C0.instruction.Operation;
import C0.struct.FunctionDef;
import C0.struct.GlobalDef;
import C0.tokenizer.StringIter;
import C0.tokenizer.Token;
import C0.tokenizer.TokenType;
import C0.tokenizer.Tokenizer;

import net.sourceforge.argparse4j.*;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentAction;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.checkerframework.checker.units.qual.A;

import javax.xml.crypto.Data;

public class App {
    public static void main(String[] args) throws CompileError, IOException {

            List<GlobalDef> globalTable;
            List<FunctionDef> functionTable;
            List<Byte> Output = new ArrayList<>();

            InputStream inputStream = new FileInputStream(args[0]);
            Scanner scanner = new Scanner(inputStream);
            var iter = new StringIter(scanner);
            Analyser analyser = new Analyser(new Tokenizer(iter));
            analyser.analyse();

            globalTable = new ArrayList<>(analyser.getGlobalTable());
            functionTable = new ArrayList<>(analyser.getFunctionTable());

            //-----
            System.out.println(globalTable.size());
            for (GlobalDef globalDef : globalTable) {
                System.out.println(globalDef.getName());
            }

            functionTable.sort(new Comparator<FunctionDef>() {
                @Override
                public int compare(FunctionDef o1, FunctionDef o2) {
                    return o1.getFunctionID() - o2.getFunctionID();
                }
            });

            //---------------------------------------
            System.out.println(functionTable.size());
            for (FunctionDef functionDef : functionTable) {
                System.out.println("------------------------");
                System.out.println(functionDef.getOffset());
                System.out.println(functionDef.getLoc_slots());
                System.out.println(functionDef.getParam_slots());
                System.out.println(functionDef.getReturn_slots());
                for(Instruction instruction:functionDef.getInstructions()){
                    System.out.print(instruction.getOpt()+" ");
                    if(instruction.getByteNum()!=0){
                        System.out.println(instruction.getX());
                    }
                    else {
                        System.out.println(" ");
                    }
                }
            }

            Output.addAll(IntToBytes(0x72303b3e));
            Output.addAll(IntToBytes(0x00000001));

            //全局量
            Output.addAll(IntToBytes(globalTable.size()));
            for(GlobalDef globalDef:globalTable){
                Output.add(ByteIntToBytes(globalDef.getIs_const()));
                if(globalDef.getByteValues()==null){
                    Output.addAll(IntToBytes(8));
                    Output.addAll(LongToBytes(0));
                }
                else{
                    Output.addAll(IntToBytes(globalDef.getName().length()));
                    Output.addAll(ListCharToBytes(globalDef.getByteValues()));
                }
            }

            //函数
            Output.addAll(IntToBytes(functionTable.size()));
            for(FunctionDef functionDef:functionTable){
                Output.addAll(IntToBytes(functionDef.getFunctionID()));
                Output.addAll(IntToBytes(functionDef.getReturn_slots()));
                Output.addAll(IntToBytes(functionDef.getParam_slots()));
                Output.addAll(IntToBytes(functionDef.getLoc_slots()));

                List<Instruction> instructions=functionDef.getInstructions();
                Output.addAll(IntToBytes(instructions.size()));

                for(Instruction instruction:instructions){
                    Output.add(ByteIntToBytes(instruction.getOpt().getByte()));
                    if(instruction.getByteNum()==4){
                        Output.addAll(IntToBytes((int)instruction.getX()));
                    }
                    else if(instruction.getByteNum()==8){
                        Output.addAll(LongToBytes(instruction.getX()));
                    }
                }
            }

            DataOutputStream outputStream=new DataOutputStream(new FileOutputStream(args[1]));
            byte[] outPutByte= new byte[Output.size()];
            for(int i=0;i<Output.size();i++){
                outPutByte[i]=Output.get(i);
            }
            outputStream.write(outPutByte);

    }


     private static List<Byte> IntToBytes(int target) {
        ArrayList<Byte> bytes = new ArrayList<>();
        int start = 8 * 3;
        for(int i = 0 ; i < 4; i++){
            bytes.add((byte) (( target >> ( start - i * 8 )) & 0xFF ));
        }
        return bytes;
    }

    private static Byte ByteIntToBytes(int target) {
        return (byte) (target  & 0xFF );
    }

    private static List<Byte> ListCharToBytes(char[] value) {
        List<Byte>  bytes=new ArrayList<>();
        for (char ch : value) {
            bytes.add((byte) (ch & 0xff));
        }
        return bytes;
    }

    private static List<Byte> LongToBytes(long target) {
        ArrayList<Byte> bytes = new ArrayList<>();
        int start = 8 * 7;
        for(int i = 0 ; i < 8; i++){
            bytes.add((byte) (( target >> ( start - i * 8 )) & 0xFF ));
        }
        return bytes;
    }
}
