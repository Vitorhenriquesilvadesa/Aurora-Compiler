package org.aurora.compiler;

import org.aurora.type.AurValue;
import org.aurora.parser.AurParsedData;
import org.aurora.parser.expression.*;
import org.aurora.parser.statement.AurExpressionStatement;
import org.aurora.parser.statement.AurStatementNode;
import org.aurora.pass.AurCompilationPass;
import org.aurora.processor.AurExpressionNodeProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AurCompiler extends AurCompilationPass<AurParsedData, AurCompiledCode>
        implements AurExpressionNodeProcessor<List<Byte>> {

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
        for (AurStatementNode expression : input.getExpressions()) {
            if (expression instanceof AurExpressionStatement) {
                AurExpressionNode statement = ((AurExpressionStatement) expression).expression;
                bytecode.addAll(statement.acceptProcessor(this));
            }
        }

        bytecode.add(AurInstructionCode.RETURN);

        return new AurCompiledCode(bytecode, indexToValue);
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
        return List.of();
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
}
