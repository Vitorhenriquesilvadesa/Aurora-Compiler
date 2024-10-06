package org.aurora.scanner;

public record Token(TokenType type, String lexeme, Object literal, int line) {

    @Override
    public String toString() {
        return "<" + type.toString() + ", " + lexeme + ">";
    }
}
