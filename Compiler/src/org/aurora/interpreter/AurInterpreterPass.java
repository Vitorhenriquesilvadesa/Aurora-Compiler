package org.aurora.interpreter;

import org.aurora.parser.ParsedData;
import org.aurora.pass.AurCompilationPass;

public class AurInterpreterPass extends AurCompilationPass<ParsedData, AurInterpretResult> {

    @Override
    public Class<ParsedData> getInputType() {
        return ParsedData.class;
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
    protected AurInterpretResult pass(ParsedData input) {
        return null;
    }
}
