package org.aurora.binary;

import org.aurora.component.AurIOComponent;
import org.aurora.exception.AurException;
import org.aurora.type.AurValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AurBytecode extends AurIOComponent<AurBytecode> {

    public final byte[] code;
    public final List<Byte> rawCode;
    public final Map<Byte, AurValue> constantTable;

    public AurBytecode(String path, List<Byte> rawCode, Map<Byte, AurValue> constantTable) {
        this.rawCode = rawCode;
        try {
            this.code = Files.readAllBytes(Path.of(path));
            this.constantTable = constantTable;
        } catch (IOException e) {
            throw new AurException("Cannot read binary file.");
        }
    }

    private AurBytecode(byte[] code, List<Byte> rawCode, Map<Byte, AurValue> constantTable) {
        this.code = code;
        this.rawCode = rawCode;
        this.constantTable = constantTable;
    }

    @Override
    public AurBytecode clone() {
        return new AurBytecode(this.code, this.rawCode, constantTable);
    }

    @Override
    public String toString() {
        return "AurBytecode [code=" + Arrays.toString(code) + "]";
    }
}
