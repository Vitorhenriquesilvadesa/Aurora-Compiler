package org.aurora.compiler;

import org.aurora.type.AurValue;
import org.aurora.component.AurIOComponent;

import java.util.List;
import java.util.Map;

public class AurCompiledCode extends AurIOComponent<AurCompiledCode> {

    public final List<Byte> code;
    public final List<Byte> rawCode;
    public final Map<Byte, AurValue> constantTable;

    public AurCompiledCode(List<Byte> code, List<Byte> rawCode, Map<Byte, AurValue> constantTable) {
        this.code = code;
        this.rawCode = rawCode;
        this.constantTable = constantTable;
    }

    @Override
    public AurCompiledCode clone() {
        return new AurCompiledCode(code, this.rawCode, constantTable);
    }
}
