package org.aurora.scanner;

import org.aurora.component.AurIOComponent;

import java.util.List;

public class ScannedData extends AurIOComponent {

    private final List<Token> tokens;

    public ScannedData(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    @Override
    public AurIOComponent clone() {
        return new ScannedData(tokens);
    }
}
