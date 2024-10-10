package org.aurora.interpreter;

import org.aurora.exception.AurException;
import org.aurora.parser.AurParsedData;
import org.aurora.parser.expression.*;
import org.aurora.parser.statement.*;
import org.aurora.pass.AurCompilationPass;
import org.aurora.processor.AurExpressionNodeProcessor;
import org.aurora.processor.AurStatementNodeProcessor;
import org.aurora.type.AurValue;
import org.aurora.type.AurValueType;

public class AurInterpretPass extends AurCompilationPass<AurParsedData, AurInterpretResult> implements AurExpressionNodeProcessor<AurValue>,
        AurStatementNodeProcessor<Void> {

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
        for (AurStatementNode statement : input.getStatements()) {
            execute(statement);
        }

        return new AurInterpretResult(0);
    }

    private void execute(AurStatementNode statement) {
        statement.acceptProcessor(this);
    }


    private AurValue evaluate(AurExpressionNode expressionNode) {
        return expressionNode.acceptProcessor(this);
    }

    @Override
    public AurValue processLiteralExpression(AurLiteralExpression expression) {
        return expression.literal.literal();
    }

    @Override
    public AurValue processBinaryExpression(AurBinaryExpression expression) {
        AurValue left = evaluate(expression.left);
        AurValue right = evaluate(expression.right);

        switch (expression.operator.type()) {
            case PLUS:
                if (checkFloatOperands(left, right)) {
                    return new AurValue((float) left.value + (float) right.value, AurValueType.FLOAT);
                } else {
                    return new AurValue((int) left.value + (int) right.value, AurValueType.INT);
                }

            case MINUS:
                if (checkFloatOperands(left, right)) {
                    return new AurValue((float) left.value - (float) right.value, AurValueType.FLOAT);
                } else {
                    return new AurValue((int) left.value - (int) right.value, AurValueType.INT);
                }

            case STAR:
                if (checkFloatOperands(left, right)) {
                    return new AurValue((float) left.value * (float) right.value, AurValueType.FLOAT);
                } else {
                    return new AurValue((int) left.value * (int) right.value, AurValueType.INT);
                }

            case SLASH:
                if (checkFloatOperands(left, right)) {
                    return new AurValue((float) left.value * (float) right.value, AurValueType.FLOAT);
                } else {
                    return new AurValue((int) left.value / (int) right.value, AurValueType.INT);
                }

            default:
                throw new AurException("Operands must be numbers.");
        }
    }

    private boolean checkFloatOperands(AurValue left, AurValue right) {
        return left.type == AurValueType.FLOAT && right.type == AurValueType.INT ||
                left.type == AurValueType.INT && right.type == AurValueType.FLOAT ||
                left.type == AurValueType.FLOAT && right.type == AurValueType.FLOAT;
    }

    @Override
    public AurValue processUnaryExpression(AurUnaryExpression expression) {
        AurValue operand = evaluate(expression.expression);

        switch (expression.operator.type()) {
            case MINUS:
                if (checkFloatOperand(operand)) {
                    return new AurValue(-(float) operand.value, AurValueType.FLOAT);
                } else if (checkIntOperand(operand)) {
                    return new AurValue(-(int) operand.value, AurValueType.INT);
                } else {
                    throw new AurException("Operands must be number.");
                }

            case MARK:
                if (checkBooleanOperand(operand)) {
                    return new AurValue(!((boolean) operand.value), AurValueType.BOOL);
                } else {
                    throw new AurException("Operand must be boolean.");
                }

            default:
                throw new AurException("Operands must be number or boolean.");
        }
    }

    @Override
    public AurValue processLogicalExpression(AurLogicalExpression expression) {
        AurValue left = evaluate(expression.left);
        AurValue right = evaluate(expression.right);

        switch (expression.operator.type()) {
            case GREATER:
                if (checkNumberOperands(left, right)) {
                    //noinspection unchecked
                    return new AurValue(((Comparable<Number>) left.value).compareTo((Number) right.value) > 0, AurValueType.BOOL);
                }
            case GREATER_EQUAL:
                if (checkNumberOperands(left, right)) {
                    //noinspection unchecked
                    return new AurValue(((Comparable<Number>) left.value).compareTo((Number) right.value) >= 0, AurValueType.BOOL);
                }
            case LESS:
                if (checkNumberOperands(left, right)) {
                    //noinspection unchecked
                    return new AurValue(((Comparable<Number>) left.value).compareTo((Number) right.value) < 0, AurValueType.BOOL);
                }
            case LESS_EQUAL:
                if (checkNumberOperands(left, right)) {
                    //noinspection unchecked
                    return new AurValue(((Comparable<Number>) left.value).compareTo((Number) right.value) <= 0, AurValueType.BOOL);
                }


            case EQUAL_EQUAL:
                return new AurValue((left.equals(right)), AurValueType.BOOL);

            case MARK_EQUAL:
                return new AurValue(!(left.equals(right)), AurValueType.BOOL);

            case AND:
                return new AurValue((boolean) left.value && (boolean) right.value, AurValueType.BOOL);

            case OR:
                return new AurValue((boolean) left.value || (boolean) right.value, AurValueType.BOOL);

            default:
                throw new AurException("Invalid logical expression.");
        }
    }

    private boolean checkNumberOperands(AurValue left, AurValue right) {
        return checkFloatOperands(left, right) || checkIntOperands(left, right);
    }

    private boolean checkIntOperands(AurValue left, AurValue right) {
        return left.type == AurValueType.INT && right.type == AurValueType.INT;
    }

    @Override
    public AurValue processGroupExpression(AurGroupExpression expression) {
        return evaluate(expression.expression);
    }

    @Override
    public AurValue processVariableGetExpression(AurVariableGetExpression expression) {
        return null;
    }


    private boolean checkIntOperand(AurValue operand) {
        return operand.type == AurValueType.INT;
    }

    private boolean checkBooleanOperand(AurValue operand) {
        return operand.type == AurValueType.BOOL;
    }

    private boolean checkFloatOperand(AurValue operand) {
        return operand.type == AurValueType.FLOAT;
    }

    @Override
    public Void processIfStatement(AurIfStatement statement) {
        AurValue condition = evaluate(statement.condition);

        if (((boolean) condition.value)) {
            execute(statement.thenStatement);
        } else if (statement.elseStatement != null) {
            execute(statement.elseStatement);
        }

        return null;
    }

    @Override
    public Void processExpressionStatement(AurExpressionStatement statement) {
        evaluate(statement.expression);
        return null;
    }

    @Override
    public Void processBodyStatement(AurBodyStatement statement) {

        for (AurStatementNode statementNode : statement.statements) {
            execute(statementNode);
        }
        return null;
    }

    @Override
    public Void processPrintStatement(PrintStatement statement) {
        AurValue value = evaluate(statement.value);
        System.out.println(stringify(value));

        return null;
    }

    @Override
    public Void processVariableDeclaration(VariableDeclarationStatement statement) {
        return null;
    }

    private String stringify(AurValue value) {
        if (value.type == AurValueType.NULL) {
            return "null";
        } else {
            return value.toString();
        }
    }
}
