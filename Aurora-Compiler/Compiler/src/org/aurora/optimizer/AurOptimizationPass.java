package org.aurora.optimizer;

import org.aurora.compiler.AurInstructionCode;
import org.aurora.parser.AurParsedData;
import org.aurora.parser.expression.*;
import org.aurora.parser.statement.*;
import org.aurora.pass.AurCompilationPass;
import org.aurora.scanner.Token;
import org.aurora.scanner.TokenType;
import org.aurora.type.AurValue;
import org.aurora.type.AurValueType;

import java.util.ArrayList;
import java.util.List;

public class AurOptimizationPass extends AurCompilationPass<AurParsedData, AurParsedData> {

    @Override
    public Class<AurParsedData> getInputType() {
        return AurParsedData.class;
    }

    @Override
    public Class<AurParsedData> getOutputType() {
        return AurParsedData.class;
    }

    @Override
    public String getDebugName() {
        return "Optimization Pass";
    }

    @Override
    protected AurParsedData pass(AurParsedData input) {
        List<AurStatementNode> statements = new ArrayList<>();

        for (AurStatementNode node : input.getStatements()) {
            statements.add(optimize(node));
        }

        return new AurParsedData(statements);
    }

    private AurStatementNode optimize(AurStatementNode node) {
        if (node instanceof AurExpressionStatement) {
            AurExpressionNode expression = ((AurExpressionStatement) node).expression;
            return new AurExpressionStatement(optimize(expression));
        }

        if (node instanceof AurIfStatement ifStatement) {
            AurExpressionNode condition = ifStatement.condition;

            if (condition instanceof AurLogicalExpression logicalCondition) {
                AurExpressionNode left = optimize(logicalCondition.left);
                AurExpressionNode right = optimize(logicalCondition.right);
                Token operator = logicalCondition.operator;

                if (operator.type() == TokenType.AND) {
                    if (left instanceof AurLiteralExpression leftLiteral) {
                        if (leftLiteral.literal.literal().type == AurValueType.BOOL &&
                                (boolean) leftLiteral.literal.literal().value) {
                            return new AurIfStatement(right, ifStatement.thenStatement, ifStatement.elseStatement);
                        } else if (leftLiteral.literal.literal().type == AurValueType.BOOL &&
                                !(boolean) leftLiteral.literal.literal().value) {
                            return ifStatement.elseStatement;
                        }
                    }
                } else if (operator.type() == TokenType.OR) {
                    if (left instanceof AurLiteralExpression leftLiteral) {
                        if (leftLiteral.literal.literal().type == AurValueType.BOOL &&
                                (boolean) leftLiteral.literal.literal().value) {
                            return ifStatement.thenStatement;  // Retorna apenas o bloco then
                        } else if (right instanceof AurLiteralExpression rightLiteral) {
                            if (rightLiteral.literal.literal().type == AurValueType.BOOL &&
                                    (boolean) rightLiteral.literal.literal().value) {
                                return ifStatement.thenStatement;  // Retorna apenas o bloco then
                            }
                        }
                    }
                }

                condition = new AurLogicalExpression(left, operator, right);
            }

            if (condition instanceof AurLiteralExpression conditionLiteral) {
                if (conditionLiteral.literal.literal().type == AurValueType.BOOL) {
                    if ((boolean) conditionLiteral.literal.literal().value) {
                        return ifStatement.thenStatement;
                    } else {
                        return ifStatement.elseStatement;
                    }
                }
            }

            return new AurIfStatement(condition, ifStatement.thenStatement, ifStatement.elseStatement);
        }

        if (node instanceof VariableDeclarationStatement statement) {
            AurExpressionNode value = optimize(statement.value);
            return new VariableDeclarationStatement(statement.type, statement.name, value);
        }

        return node;
    }


    private AurExpressionNode optimize(AurExpressionNode expression) {
        if (expression instanceof AurBinaryExpression binary) {
            return tryResolveBinary(binary);
        }
        if (expression instanceof AurLogicalExpression logical) {
            return tryResolveLogical(logical);
        }
        if (expression instanceof AurGroupExpression group) {
            return tryResolveGroup(group);
        }

        return expression;
    }

    private AurExpressionNode tryResolveGroup(AurGroupExpression group) {
        AurExpressionNode optimizedExpression = optimize(group.expression);

        if (optimizedExpression instanceof AurLiteralExpression) {
            return optimizedExpression;
        }

        return group;
    }

    private AurExpressionNode tryResolveBinary(AurBinaryExpression binary) {
        AurExpressionNode left = optimize(binary.left);
        AurExpressionNode right = optimize(binary.right);

        if (left instanceof AurLiteralExpression leftLiteral && right instanceof AurLiteralExpression rightLiteral) {
            AurValue a = leftLiteral.literal.literal();
            AurValue b = rightLiteral.literal.literal();

            AurValue resultValue;

            switch (binary.operator.type()) {
                case TokenType.PLUS: {
                    resultValue = new AurValue((int) a.value + (int) b.value, AurValueType.INT);
                    break;
                }

                case TokenType.MINUS: {
                    resultValue = new AurValue((int) a.value - (int) b.value, AurValueType.INT);
                    break;
                }

                case TokenType.STAR: {
                    resultValue = new AurValue((int) a.value * (int) b.value, AurValueType.INT);
                    break;
                }
                case TokenType.SLASH: {
                    resultValue = new AurValue((int) a.value / (int) b.value, AurValueType.INT);
                    break;
                }

                default:
                    return binary;
            }

            switch (resultValue.type) {
                case AurValueType.INT:
                    return new AurLiteralExpression(new Token(TokenType.INT, null, resultValue, 0));
                case AurValueType.FLOAT:
                    return new AurLiteralExpression(new Token(TokenType.FLOAT, null, resultValue, 0));
                case AurValueType.CHAR:
                    return new AurLiteralExpression(new Token(TokenType.CHAR, null, resultValue, 0));
                case AurValueType.STRING:
                    return new AurLiteralExpression(new Token(TokenType.STRING, null, resultValue, 0));
            }
        }

        return new AurBinaryExpression(left, binary.operator, right);
    }

    private AurExpressionNode tryResolveLogical(AurLogicalExpression logical) {
        AurExpressionNode left = optimize(logical.left);
        AurExpressionNode right = optimize(logical.right);

        if (left instanceof AurLiteralExpression leftLiteral && right instanceof AurLiteralExpression rightLiteral) {
            boolean result;

            switch (logical.operator.type()) {
                case TokenType.AND: {
                    boolean a = (Boolean) leftLiteral.literal.literal().value;
                    boolean b = (Boolean) rightLiteral.literal.literal().value;
                    result = a && b;
                    break;
                }
                case TokenType.OR: {
                    boolean a = (Boolean) leftLiteral.literal.literal().value;
                    boolean b = (Boolean) rightLiteral.literal.literal().value;
                    result = a || b;
                    break;
                }
                case TokenType.GREATER: {
                    int a = (Integer) leftLiteral.literal.literal().value;
                    int b = (Integer) rightLiteral.literal.literal().value;
                    result = a > b;
                    break;
                }
                case TokenType.GREATER_EQUAL: {
                    int a = (Integer) leftLiteral.literal.literal().value;
                    int b = (Integer) rightLiteral.literal.literal().value;
                    result = a >= b;
                    break;
                }
                case TokenType.LESS: {
                    int a = (Integer) leftLiteral.literal.literal().value;
                    int b = (Integer) rightLiteral.literal.literal().value;
                    result = a < b;
                    break;
                }
                case TokenType.LESS_EQUAL: {
                    int a = (Integer) leftLiteral.literal.literal().value;
                    int b = (Integer) rightLiteral.literal.literal().value;
                    result = a <= b;
                    break;
                }
                case TokenType.EQUAL_EQUAL: {
                    Object a = leftLiteral.literal.literal().value;
                    Object b = rightLiteral.literal.literal().value;
                    result = a.equals(b);
                    break;
                }
                case TokenType.MARK_EQUAL: {
                    Object a = leftLiteral.literal.literal().value;
                    Object b = rightLiteral.literal.literal().value;
                    result = !a.equals(b);
                    break;
                }
                default:
                    return logical;
            }

            return new AurLiteralExpression(new Token(result ? TokenType.TRUE : TokenType.FALSE, null, new AurValue(result, AurValueType.BOOL), 0));
        }

        return new AurLogicalExpression(left, logical.operator, right);
    }
}
