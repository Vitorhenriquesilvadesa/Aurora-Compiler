package org.aurora.scanner;

import org.aurora.pass.AurCompilationPass;
import org.aurora.util.AurFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.aurora.scanner.TokenType.*;

public class AurScanPass extends AurCompilationPass<AurFile, ScannedData> {

    private int line;
    private List<Token> tokens;
    private String source;
    private int start;
    private int current;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("public", PUBLIC);
        keywords.put("locked", LOCKED);
    }

    @Override
    public Class<AurFile> getInputType() {
        return AurFile.class;
    }

    @Override
    public String getDebugName() {
        return "Scan Pass";
    }

    @Override
    protected ScannedData pass(AurFile input) {
        return scanTokens(input);
    }

    private ScannedData scanTokens(AurFile input) {
        resetInternalState(input);

        while (!isAtEnd()) {
            syncCursors();
            scanToken();
        }

        return new ScannedData(tokens);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void scanToken() {
        char c = advance();

        switch (c) {

            case '+':
                makeToken(PLUS);
                break;

            case '-':
                makeToken(MINUS);
                break;

            case '*':
                makeToken(STAR);
                break;

            case '/':
                makeToken(SLASH);
                break;

            case ' ':
            case '\t':
            case '\r':
                break;

            case '\n':
                line++;
                break;

            default:

                if (isDigit(c)) {
                    number();
                    break;
                }

                if(isAlpha(c)) {
                    identifier();
                    break;
                }
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);

        makeToken(keywords.getOrDefault(text, IDENTIFIER));
    }

    private void number() {
        while (isDigit(peek()) || peek() == '_') advance();
        if (peek() == '.' && isDigit(peekNext())) {
            do {
                advance();
            } while (isDigit(peek()));

            makeToken(FLOAT, Float.parseFloat(source.substring(start, current).replaceAll("_", "")));

        } else {
            makeToken(INT, Integer.parseInt(source.substring(start, current).replaceAll("_", "")));
        }
    }

    private char peekNext() {
        return source.charAt(current + 1);
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isAlpha(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean match(char c) {
        if (isAtEnd()) return false;
        if (peek() != c) return false;
        advance();
        return true;
    }

    private char peek() {
        return source.charAt(current);
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void syncCursors() {
        start = current;
    }

    private void resetInternalState(AurFile file) {
        start = 0;
        current = 0;
        line = 1;
        tokens = new ArrayList<>();
        source = file.getSource();
    }

    private void makeToken(TokenType type) {
        makeToken(type, null);
    }

    private void makeToken(TokenType type, Object literal) {
        String lexeme = source.substring(start, current);
        makeToken(type, lexeme, literal);
    }

    private void makeToken(TokenType type, String lexeme, Object literal) {
        Token token = new Token(type, lexeme, literal, line);
        tokens.add(token);
    }
}
