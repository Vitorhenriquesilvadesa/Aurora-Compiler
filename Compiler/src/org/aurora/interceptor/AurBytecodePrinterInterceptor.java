package org.aurora.interceptor;

import org.aurora.compiler.AurCompiledCode;
import org.aurora.compiler.AurInstructionCode;
import org.aurora.exception.AurException;
import org.aurora.parser.AurParsedData;
import org.aurora.type.AurValueType;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

public class AurBytecodePrinterInterceptor implements AurPassiveInterceptor<AurParsedData, AurCompiledCode> {

    private int depth = 0;
    private int tabSize = 4;

    @Override
    public void beforeState(AurParsedData input) {

    }

    @Override
    public void afterState(AurCompiledCode input) {

        try (PrintStream writer = System.out) {
            writer.println("MAIN:");
            beginScope();

            for (int i = 0; i < input.code.size(); i++) {
                switch (input.code.get(i)) {
                    case AurInstructionCode.RETURN:
                        writer.println(indent() + "RETURN");
                        break;

                    case AurInstructionCode.LOAD_CONST:
                        writer.print(indent() + "LOAD_CONST\t");
                        i++;
                        writer.println(indent() + input.code.get(i));
                        break;

                    case AurInstructionCode.ADD:
                        writer.println(indent() + "ADD");
                        break;

                    case AurInstructionCode.SUB:
                        writer.println(indent() + "SUB");
                        break;

                    case AurInstructionCode.MUL:
                        writer.println(indent() + "MUL");
                        break;

                    case AurInstructionCode.DIV:
                        writer.println(indent() + "DIV");
                        break;

                    case AurInstructionCode.INVERSE:
                        writer.println(indent() + "INVERSE");
                        break;

                    case AurInstructionCode.NEGATE:
                        writer.println(indent() + "NEGATE");
                        break;

                    default:
                        writer.println(indent() + input.code.get(i));
                        break;
                }
            }

            endScope();

            writeConstantTable(input, writer);

        } catch (IOException e) {
            throw new AurException("Could not write .abc file.");
        }
    }

    private void writeConstantTable(AurCompiledCode code, PrintStream writer) throws IOException {

        writer.println();
        writer.println(indent() + "TABLE:");

        beginScope();

        for (byte i = 0; i < code.constantTable.size(); i++) {
            byte type = code.constantTable.values().stream().toList().get(i).type;

            writer.print(indent() + type);
            writer.print(": ");
            writer.println(sameLineIndent("" + type) + code.constantTable.keySet().stream().toList().get(i));
        }

        endScope();
    }

    private String sameLineIndent(String value) {
        return " ".repeat((depth + 1) * tabSize - value.length());
    }

    private String indent() {
        return " ".repeat(tabSize * depth);
    }

    private void beginScope() {
        depth++;
    }

    private void endScope() {
        depth--;
    }
}
