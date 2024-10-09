package org.aurora.parser;

import org.aurora.exception.AurParseException;
import org.aurora.parser.expression.*;
import org.aurora.parser.statement.AurBodyStatement;
import org.aurora.parser.statement.AurExpressionStatement;
import org.aurora.parser.statement.AurIfStatement;
import org.aurora.parser.statement.AurStatementNode;
import org.aurora.pass.AurCompilationPass;
import org.aurora.scanner.AurScannedData;
import org.aurora.scanner.Token;
import org.aurora.scanner.TokenType;

import java.util.ArrayList;
import java.util.List;

import static org.aurora.scanner.TokenType.*;

public class AurParsePass extends AurCompilationPass<AurScannedData, AurParsedData> {


    private List<Token> tokens;
    private int current;

    @Override
    public Class<AurScannedData> getInputType() {
        return AurScannedData.class;
    }

    @Override
    public Class<AurParsedData> getOutputType() {
        return AurParsedData.class;
    }

    @Override
    public String getDebugName() {
        return "Parse Pass";
    }

    @Override
    protected AurParsedData pass(AurScannedData input) {
        return parseTokens(input);
    }

    private void resetInternalState(AurScannedData input) {
        tokens = input.getTokens();
        current = 0;
    }

    private AurParsedData parseTokens(AurScannedData input) {
        resetInternalState(input);

        List<AurStatementNode> statements = new ArrayList<>();

        while (!isAtEnd()) {
            AurStatementNode statementNode = statement();
            statements.add(statementNode);
        }

        return new AurParsedData(statements);
    }

    private AurStatementNode statement() {
        if (match(IF)) return ifStatement();
        if (match(LEFT_BRACE)) return bodyStatement();

        return expressionStatement();
    }

    private AurStatementNode bodyStatement() {
        List<AurStatementNode> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE)) {
            statements.add(statement());
        }

        consume(RIGHT_BRACE, "Expect '}' after body.");

        return new AurBodyStatement(statements);
    }

    private AurStatementNode expressionStatement() {
        AurExpressionNode expression = expression();
        return new AurExpressionStatement(expression);
    }

    private AurStatementNode ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        AurExpressionNode condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after 'if' condition.");
        AurStatementNode thenBranch = statement();
        AurStatementNode elseBranch = null;

        if (match(ELSE)) {
            elseBranch = statement();
        }

        return new AurIfStatement(condition, thenBranch, elseBranch);
    }

    private AurExpressionNode expression() {
        return or();
    }

    private AurExpressionNode or() {
        AurExpressionNode expressionNode = and();

        while (match(OR)) {
            Token operator = previous();
            AurExpressionNode right = and();
            expressionNode = new AurLogicalExpression(expressionNode, operator, right);
        }

        return expressionNode;
    }

    private AurExpressionNode and() {
        AurExpressionNode expressionNode = equality();

        while (match(AND)) {
            Token operator = previous();
            AurExpressionNode right = equality();
            expressionNode = new AurLogicalExpression(expressionNode, operator, right);
        }

        return expressionNode;
    }

    private AurExpressionNode equality() {

        AurExpressionNode expressionNode = comparison();

        while (match(EQUAL_EQUAL, MARK_EQUAL)) {
            Token operator = previous();
            AurExpressionNode right = comparison();
            expressionNode = new AurLogicalExpression(expressionNode, operator, right);
        }

        return expressionNode;
    }

    private AurExpressionNode comparison() {

        AurExpressionNode expressionNode = term();

        if (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
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
        if (match(INT, FLOAT, TRUE, FALSE, STRING, CHAR)) {
            return new AurLiteralExpression(previous());
        }

        if (match(LEFT_PAREN)) {
            return group();
        }

        error("Invalid expression");
        return null;
    }

    private AurExpressionNode group() {
        Token paren = previous();
        AurExpressionNode expressionNode = expression();
        consume(RIGHT_PAREN, "Expect ')' after group expression.");

        return new AurGroupExpression(paren, expressionNode);
    }

    private void consume(TokenType type, String message) {
        if (!match(type)) {
            error(message);
        }
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
        if (isAtEnd()) return false;
        return peek().type() == type;
    }

    private Token peek() {
        return tokens.get(current);
    }
}
