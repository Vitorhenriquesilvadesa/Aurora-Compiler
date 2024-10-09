package org.aurora.compiler;

import org.aurora.parser.statement.*;
import org.aurora.processor.AurStatementNodeProcessor;
import org.aurora.type.AurValue;
import org.aurora.parser.AurParsedData;
import org.aurora.parser.expression.*;
import org.aurora.pass.AurCompilationPass;
import org.aurora.processor.AurExpressionNodeProcessor;

import java.util.*;

public class AurCompilePass extends AurCompilationPass<AurParsedData, AurCompiledCode>
        implements AurExpressionNodeProcessor<List<Byte>>, AurStatementNodeProcessor<List<Byte>> {

    private final Map<AurValue, Byte> constantTable = new HashMap<>();
    private final Map<Byte, AurValue> indexToValue = new HashMap<>();
    private byte currentConstantIndex = 0;

    @Override
    public Class<AurParsedData> getInputType() {
        return AurParsedData.class;
    }

    @Override
    public Class<AurCompiledCode> getOutputType() {
        return AurCompiledCode.class;
    }

    @Override
    public String getDebugName() {
        return "Compile Pass";
    }

    @Override
    protected AurCompiledCode pass(AurParsedData input) {
        List<Byte> bytecode = new ArrayList<>();
        for (AurStatementNode statement : input.getStatements()) {
            bytecode.addAll(statement.acceptProcessor(this));
        }

        bytecode.add(AurInstructionCode.RETURN);

        return new AurCompiledCode(bytecode, bytecode, indexToValue);
    }

    private List<Byte> generateBytecode(AurExpressionNode expression) {
        return expression.acceptProcessor(this);
    }

    @Override
    public List<Byte> processLiteralExpression(AurLiteralExpression expression) {
        List<Byte> result = new ArrayList<>();

        byte constantIndex = writeConstant(expression.literal.literal());

        emitByte(AurInstructionCode.LOAD_CONST, result);
        emitByte(constantIndex, result);

        return result;
    }

    @Override
    public List<Byte> processBinaryExpression(AurBinaryExpression expression) {
        List<Byte> leftBytecode = generateBytecode(expression.left);
        List<Byte> rightBytecode = generateBytecode(expression.right);
        List<Byte> result = new ArrayList<>();
        result.addAll(leftBytecode);
        result.addAll(rightBytecode);

        switch (expression.operator.type()) {
            case PLUS:
                emitByte(AurInstructionCode.ADD, result);
                break;

            case MINUS:
                emitByte(AurInstructionCode.SUB, result);
                break;

            case STAR:
                emitByte(AurInstructionCode.MUL, result);
                break;

            case SLASH:
                emitByte(AurInstructionCode.DIV, result);
                break;
        }

        return result;
    }

    @Override
    public List<Byte> processUnaryExpression(AurUnaryExpression expression) {
        List<Byte> expressionCode = generateBytecode(expression.expression);
        List<Byte> result = new ArrayList<>(expressionCode);

        switch (expression.operator.type()) {
            case MINUS:
                emitByte(AurInstructionCode.NEGATE, result);
                break;

            case MARK:
                emitByte(AurInstructionCode.INVERSE, result);
                break;
        }

        return result;
    }

    @Override
    public List<Byte> processLogicalExpression(AurLogicalExpression expression) {
        List<Byte> result = generateBytecode(expression.left);
        result.addAll(generateBytecode(expression.right));

        switch (expression.operator.type()) {
            case GREATER:
                emitByte(AurInstructionCode.GREATER, result);
                break;

            case GREATER_EQUAL:
                emitByte(AurInstructionCode.GREATER_EQUAL, result);
                break;

            case LESS:
                emitByte(AurInstructionCode.LESS, result);
                break;

            case LESS_EQUAL:
                emitByte(AurInstructionCode.LESS_EQUAL, result);
                break;

            case EQUAL_EQUAL:
                emitByte(AurInstructionCode.EQUAL_EQUAL, result);
                break;

            case MARK_EQUAL:
                emitByte(AurInstructionCode.MARK_EQUAL, result);
                break;

            case AND:
                emitByte(AurInstructionCode.AND, result);
                break;

            case OR:
                emitByte(AurInstructionCode.OR, result);
                break;
        }

        return result;
    }

    @Override
    public List<Byte> processGroupExpression(AurGroupExpression expression) {
        return generateBytecode(expression.expression);
    }

    private byte writeConstant(AurValue constant) {
        if (constantTable.containsKey(constant)) {
            return constantTable.get(constant);
        }

        constantTable.put(constant, currentConstantIndex);
        indexToValue.put(currentConstantIndex, constant);
        return currentConstantIndex++;
    }

    private void emitByte(Byte instruction, List<Byte> code) {
        code.add(instruction);
    }

    @Override
    public List<Byte> processIfStatement(AurIfStatement statement) {

        List<Byte> result = new ArrayList<>(statement.condition.acceptProcessor(this));

        List<Byte> thenBranch = statement.thenStatement.acceptProcessor(this);
        List<Byte> elseBranch = new ArrayList<>();

        if (statement.elseStatement != null) {
            elseBranch = statement.elseStatement.acceptProcessor(this);
        }

        short ifInstructionCount = (short) (thenBranch.size() + 3);
        byte ifHighByte = (byte) (ifInstructionCount >> 8);
        byte ifLowByte = (byte) (ifInstructionCount & 0xFF);

        emitByte(AurInstructionCode.JUMP_IF_FALSE, result);
        emitByte(ifLowByte, result);
        emitByte(ifHighByte, result);
        result.addAll(thenBranch);

        short elseInstructionCount = (short) (elseBranch.size());
        byte elseHighByte = (byte) (elseInstructionCount >> 8);
        byte elseLowByte = (byte) (elseInstructionCount & 0xFF);

        emitByte(AurInstructionCode.JUMP, result);
        emitByte(elseLowByte, result);
        emitByte(elseHighByte, result);

        result.addAll(elseBranch);

        return result;
    }

    @Override
    public List<Byte> processExpressionStatement(AurExpressionStatement statement) {
        return statement.expression.acceptProcessor(this);
    }

    @Override
    public List<Byte> processBodyStatement(AurBodyStatement statement) {
        List<Byte> result = new ArrayList<>();

        for (AurStatementNode statementNode : statement.statements) {
            result.addAll(statementNode.acceptProcessor(this));
        }

        return result;
    }

    @Override
    public List<Byte> processPrintStatement(PrintStatement statement) {
        List<Byte> result = new ArrayList<>(generateBytecode(statement.value));
        emitByte(AurInstructionCode.PRINT, result);

        return result;
    }
}