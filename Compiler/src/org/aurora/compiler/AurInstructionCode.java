package org.aurora.compiler;

public final class AurInstructionCode {

    private AurInstructionCode() {
    }

    public static final byte RETURN = 0;
    public static final byte LOAD_CONST = 1;
    public static final byte ADD = 2;
    public static final byte SUB = 3;
    public static final byte DIV = 4;
    public static final byte MUL = 5;
    public static final byte NEGATE = 6;
    public static final byte INVERSE = 7;
}
