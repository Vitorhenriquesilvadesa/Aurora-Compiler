package org.aurora.scanner;

import org.aurora.component.AurIOComponent;

import java.util.List;

public class AurScannedData extends AurIOComponent<AurScannedData> {

    private final List<Token> tokens;

    public AurScannedData(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    @Override
    public AurScannedData clone() {
        return new AurScannedData(tokens);
    }
}
