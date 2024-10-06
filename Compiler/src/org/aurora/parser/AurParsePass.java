package org.aurora.parser;

import org.aurora.exception.AurParseException;
import org.aurora.parser.expression.*;
import org.aurora.pass.AurCompilationPass;
import org.aurora.scanner.ScannedData;
import org.aurora.scanner.Token;
import org.aurora.scanner.TokenType;

import java.util.ArrayList;
import java.util.List;

import static org.aurora.scanner.TokenType.*;

public class AurParsePass extends AurCompilationPass<ScannedData, ParsedData> {


    private List<Token> tokens;
    private int current;

    @Override
    public Class<ScannedData> getInputType() {
        return ScannedData.class;
    }

    @Override
    public Class<ParsedData> getOutputType() {
        return ParsedData.class;
    }

    @Override
    public String getDebugName() {
        return "Parse Pass";
    }

    @Override
    protected ParsedData pass(ScannedData input) {
        return parseTokens(input);
    }

    private void resetInternalState(ScannedData input) {
        tokens = input.getTokens();
        current = 0;
    }

    private ParsedData parseTokens(ScannedData input) {
        resetInternalState(input);

        List<AurExpressionNode> expressions = new ArrayList<>();

        while (!isAtEnd()) {
            AurExpressionNode expressionNode = expression();
            expressions.add(expressionNode);
        }

        return new ParsedData(expressions);
    }

    private AurExpressionNode expression() {
        return or();
    }

    private AurExpressionNode or() {
        AurExpressionNode expressionNode = and();

        while(match(OR)) {
            Token operator = previous();
            AurExpressionNode right = and();
            expressionNode = new AurLogicalExpression(expressionNode, operator, right);
        }

        return expressionNode;
    }

    private AurExpressionNode and() {
        AurExpressionNode expressionNode = equality();

        while(match(AND)) {
            Token operator = previous();
            AurExpressionNode right = equality();
            expressionNode = new AurLogicalExpression(expressionNode, operator, right);
        }

        return expressionNode;
    }

    private AurExpressionNode equality() {

        AurExpressionNode expressionNode = comparison();

        while(match(EQUAL_EQUAL, MARK_EQUAL)) {
            Token operator = previous();
            AurExpressionNode right = comparison();
            expressionNode = new AurLogicalExpression(expressionNode, operator, right);
        }

        return expressionNode;
    }

    private AurExpressionNode comparison() {

        AurExpressionNode expressionNode = term();

        if(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            AurExpressionNode right = term();
            expressionNode = new AurLogicalExpression(expressionNode, operator, right);
        }

        return expressionNode;
    }

    private AurExpressionNode term() {
        AurExpressionNode expressionNode = factor();

        while (match(PLUS, MINUS)) {
            Token operator = previous();
            AurExpressionNode right = factor();
            expressionNode = new AurBinaryExpression(expressionNode, operator, right);
        }

        return expressionNode;
    }

    private AurExpressionNode factor() {
        AurExpressionNode expressionNode = unary();

        while (match(STAR, SLASH)) {
            Token operator = previous();
            AurExpressionNode right = factor();
            expressionNode = new AurBinaryExpression(expressionNode, operator, right);
        }

        return expressionNode;
    }

    private AurExpressionNode unary() {
        if (match(MINUS, MARK)) {
            Token operator = previous();
            AurExpressionNode expressionNode = unary();
            return new AurUnaryExpression(operator, expressionNode);
        }

        return literal();
    }

    private AurExpressionNode literal() {
        if (match(INT, FLOAT)) {
            return new AurLiteralExpression(previous());
        }

        error("Invalid expression");
        return null;
    }

    private void error(String message) {
        throw new AurParseException(message);
    }

    private boolean isAtEnd() {
        return peek().type() == TokenType.EOF;
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private void advance() {
        current++;
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(TokenType type) {
        return peek().type() == type;
    }

    private Token peek() {
        return tokens.get(current);
    }
}
