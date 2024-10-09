package org.aurora.interpreter;

import org.aurora.parser.AurParsedData;
import org.aurora.pass.AurCompilationPass;

public class AurInterpretPass extends AurCompilationPass<AurParsedData, AurInterpretResult> {

    @Override
    public Class<AurParsedData> getInputType() {
        return AurParsedData.class;
    }

    @Override
    public Class<AurInterpretResult> getOutputType() {
        return AurInterpretResult.class;
    }

    @Override
    public String getDebugName() {
        return "Interpreter Pass";
    }

    @Override
    protected AurInterpretResult pass(AurParsedData input) {
        return null;
    }
}
