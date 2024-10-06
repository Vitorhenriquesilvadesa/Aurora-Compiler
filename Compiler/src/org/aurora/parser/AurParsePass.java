package org.aurora.parser;

import org.aurora.pass.AurCompilationPass;
import org.aurora.scanner.ScannedData;

public class AurParsePass extends AurCompilationPass<ScannedData, ParsedData> {

    @Override
    public Class<ScannedData> getInputType() {
        return ScannedData.class;
    }

    @Override
    public String getDebugName() {
        return "Parse Pass";
    }

    @Override
    protected ParsedData pass(ScannedData input) {
        return null;
    }
}
