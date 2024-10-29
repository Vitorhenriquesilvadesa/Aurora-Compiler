package org.aurora.compiler;

import org.aurora.component.AurNullIOComponent;
import org.aurora.exception.AurException;
import org.aurora.parser.AurParsedData;
import org.aurora.parser.expression.*;
import org.aurora.parser.statement.*;
import org.aurora.pass.AurCompilationPass;
import org.aurora.processor.AurExpressionNodeProcessor;
import org.aurora.processor.AurStatementNodeProcessor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class AurCSharpCompilerPass extends AurCompilationPass<AurParsedData, AurNullIOComponent>
        implements AurExpressionNodeProcessor<String>, AurStatementNodeProcessor<String> {

    private PrintWriter writer;
    private final String outPath = "project/build/test.cs";
    private int tabSize = 4;
    private int indentation = 0;

    public AurCSharpCompilerPass() {
        try {
            writer = new PrintWriter(new FileOutputStream(outPath));
        } catch (IOException e) {
            throw new AurException(e.getMessage());
        }
    }

    @Override
    public Class<AurParsedData> getInputType() {
        return AurParsedData.class;
    }

    @Override
    public Class<AurNullIOComponent> getOutputType() {
        return AurNullIOComponent.class;
    }

    @Override
    public String getDebugName() {
        return "C# Compiler";
    }

    @Override
    protected AurNullIOComponent pass(AurParsedData input) {

        writeLine("using System;\n");
        writeLine("using System.Collections;\n");
        writeLine("using System.Collections.Generic;\n");
        writeLine("\n");

        beginMainMethod();

        if (!input.getStatements().isEmpty()) {
            for (AurStatementNode statement : input.getStatements()) {
                if (statement != null) {
                    writeLine(evaluate(statement));
                }
            }
        }

        endMainMethod();

        writer.close();

        return new AurNullIOComponent();
    }

    private void endMainMethod() {
        endScope();
        writeLine("}\n");
        endScope();
        writeLine("}");
    }

    private void beginMainMethod() {
        writeLine("public class Program \n{\n");
        beginScope();
        writeLine("public static void Main(string[] args)\n");
        writeLine("{\n");
        beginScope();
    }

    private void writeLine(String value) {
        writer.print(indent() + value);
    }

    private String indent() {
        return " ".repeat(indentation * tabSize);
    }

    private String evaluate(AurExpressionNode expression) {
        return expression.acceptProcessor(this);
    }

    private String evaluate(AurStatementNode statement) {
        return statement.acceptProcessor(this);
    }

    @Override
    public String processLiteralExpression(AurLiteralExpression expression) {
        return expression.literal.literal().toString();
    }

    @Override
    public String processBinaryExpression(AurBinaryExpression expression) {
        StringBuilder sb = new StringBuilder();

        String left = evaluate(expression.left);
        String right = evaluate(expression.right);
        String operator = expression.operator.lexeme();

        sb.append(left).append(" ").append(operator).append(" ").append(right);

        return sb.toString();
    }

    @Override
    public String processUnaryExpression(AurUnaryExpression expression) {
        StringBuilder sb = new StringBuilder();

        String right = evaluate(expression.expression);
        String operator = expression.operator.lexeme();

        sb.append(operator).append(right);

        return sb.toString();
    }

    @Override
    public String processLogicalExpression(AurLogicalExpression expression) {
        StringBuilder sb = new StringBuilder();

        String left = evaluate(expression.left);
        String right = evaluate(expression.right);
        String operator = expression.operator.lexeme();

        switch (expression.operator.type()) {
            case AND:
                sb.append(left).append(" ").append("&&").append(" ").append(right);
                break;

            case OR:
                sb.append(left).append(" ").append("||").append(" ").append(right);
                break;

            default:
                sb.append(left).append(" ").append(operator).append(" ").append(right);
                break;
        }

        return sb.toString();
    }

    @Override
    public String processGroupExpression(AurGroupExpression expression) {
        StringBuilder sb = new StringBuilder();

        String solvedExpression = evaluate(expression.expression);
        sb.append("(").append(solvedExpression).append(")");

        return sb.toString();
    }

    @Override
    public String processVariableGetExpression(AurVariableGetExpression expression) {
        return expression.name.lexeme();
    }

    @Override
    public String processAssignmentExpression(AurAssignmentExpression expression) {
        StringBuilder sb = new StringBuilder();

        String variableName = expression.name.lexeme();
        String value = evaluate(expression.value);

        sb.append(variableName).append(" = ").append(value);

        return sb.toString();
    }

    @Override
    public String processIfStatement(AurIfStatement statement) {
        StringBuilder sb = new StringBuilder();

        sb.append("if (");
        sb.append(evaluate(statement.condition));
        sb.append(")\n");

        sb.append(evaluate(statement.thenStatement));

        if (statement.elseStatement != null) {
            sb.append(indent()).append("else\n");
            sb.append(evaluate(statement.elseStatement));
        }

        return sb.toString();
    }

    @Override
    public String processExpressionStatement(AurExpressionStatement statement) {
        return evaluate(statement.expression);
    }

    @Override
    public String processBodyStatement(AurBodyStatement statement) {
        StringBuilder sb = new StringBuilder();

        sb.append(indent()).append("{").append("\n");
        beginScope();
        for (AurStatementNode statementNode : statement.statements) {
            sb.append(indent()).append(evaluate(statementNode)).append("\n");
        }
        endScope();
        sb.append(indent()).append("}\n");

        return sb.toString();
    }

    @Override
    public String processPrintStatement(PrintStatement statement) {
        StringBuilder sb = new StringBuilder();

        sb.append("Console.WriteLine(\"");
        sb.append(evaluate(statement.value));
        sb.append("\");");

        return sb.toString();
    }

    @Override
    public String processVariableDeclaration(VariableDeclarationStatement statement) {
        StringBuilder sb = new StringBuilder();

        String variableName = statement.name.lexeme();
        String value = evaluate(statement.value);

        sb.append(statement.type.lexeme()).append(" ").append(variableName).append(" = ")
                .append(value).append(";");

        return sb.toString();
    }

    @Override
    public String processWhileStatement(AurWhileStatement statement) {
        StringBuilder sb = new StringBuilder();

        sb.append("while (");
        sb.append(evaluate(statement.condition));
        sb.append(")");

        sb.append("\n");
        sb.append(evaluate(statement.body));
        return sb.toString();
    }

    private void beginScope() {
        indentation++;
    }

    private void endScope() {
        indentation--;
    }
}
