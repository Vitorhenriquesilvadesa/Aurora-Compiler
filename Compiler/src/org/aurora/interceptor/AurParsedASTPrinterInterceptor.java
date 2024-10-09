package org.aurora.interceptor;

import org.aurora.parser.AurParsedData;
import org.aurora.parser.expression.*;
import org.aurora.parser.statement.AurBodyStatement;
import org.aurora.parser.statement.AurExpressionStatement;
import org.aurora.parser.statement.AurIfStatement;
import org.aurora.parser.statement.AurStatementNode;
import org.aurora.processor.AurExpressionNodeProcessor;
import org.aurora.processor.AurStatementNodeProcessor;
import org.aurora.scanner.AurScannedData;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class AurParsedASTPrinterInterceptor implements AurPassiveInterceptor<AurScannedData, AurParsedData>, AurExpressionNodeProcessor<String>, AurStatementNodeProcessor<String> {

    private int depth = 0;
    private final int tabSize = 2;

    @Override
    public void beforeState(AurScannedData input) {

    }

    @Override
    public void afterState(AurParsedData input) {

        try (FileWriter writer = new FileWriter("/home/vitor/IdeaProjects/Aurora/res/test.xml")) {
            PrintWriter printer = new PrintWriter(writer);

            printer.println("<Program>");
            System.out.println("<Program>");
            beginScope();

            for (AurStatementNode statementNode : input.getExpressions()) {
                String formatedExpression = format(statementNode);
                System.out.println(formatedExpression);
                printer.println(formatedExpression);
            }

            printer.print("</Program>");
            System.out.println("</Program>");
            endScope();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String format(AurExpressionNode expression) {
        return expression.acceptProcessor(this);
    }

    private String format(AurStatementNode statement) {
        return statement.acceptProcessor(this);
    }

    @Override
    public String processLiteralExpression(AurLiteralExpression expression) {
        return indent() + "<Literal>" + expression.literal.literal() + "</Literal>" + "\n";
    }

    @Override
    public String processBinaryExpression(AurBinaryExpression expression) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent()).append("<Binary>");
        newLine(sb);
        beginScope();

        sb.append(format(expression.left));

        sb.append(indent()).append("<Operator>").append(expression.operator.type().toString()).append("</Operator>");
        newLine(sb);

        sb.append(format(expression.right));

        endScope();
        sb.append(indent()).append("</Binary>");
        newLine(sb);

        return sb.toString();
    }

    @Override
    public String processUnaryExpression(AurUnaryExpression expression) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent()).append("<Unary>");
        newLine(sb);
        beginScope();

        sb.append(indent()).append("</Operator ").append(expression.operator.type().toString()).append(">");
        newLine(sb);

        sb.append(format(expression.expression));

        endScope();
        sb.append(indent()).append("</Unary>");
        newLine(sb);

        return sb.toString();
    }

    @Override
    public String processLogicalExpression(AurLogicalExpression expression) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent()).append("<Logical>");
        newLine(sb);
        beginScope();

        sb.append(format(expression.left));

        sb.append(indent()).append("<Comparator>").append(expression.operator.type().toString()).append("</Comparator>");
        newLine(sb);

        sb.append(format(expression.right));

        endScope();
        sb.append(indent()).append("</Logical>");
        newLine(sb);

        return sb.toString();
    }

    @Override
    public String processGroupExpression(AurGroupExpression expression) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent()).append("<Group>");
        newLine(sb);
        beginScope();

        sb.append(format(expression.expression));

        endScope();
        sb.append(indent()).append("</Group>");
        newLine(sb);

        return sb.toString();
    }

    private void beginScope() {
        depth += tabSize;
    }

    private void endScope() {
        depth -= tabSize;
    }

    private String indent() {
        return " ".repeat(depth);
    }

    private void newLine(StringBuilder sb) {
        sb.append(System.lineSeparator());
    }

    @Override
    public String processIfStatement(AurIfStatement statement) {
        StringBuilder sb = new StringBuilder();

        sb.append(indent()).append("<IfStatement>");
        newLine(sb);
        beginScope();

        sb.append(indent()).append("<Condition>");
        beginScope();
        newLine(sb);
        sb.append(format(statement.condition));

        endScope();
        sb.append(indent()).append("</Condition>");
        newLine(sb);

        sb.append(indent()).append("<Then>");
        beginScope();
        newLine(sb);
        sb.append(format(statement.thenStatement));

        endScope();
        sb.append(indent()).append("</Then>");
        newLine(sb);


        if (statement.elseStatement != null) {
            sb.append(indent()).append("<Else>");
            beginScope();
            newLine(sb);
            sb.append(format(statement.elseStatement));

            endScope();
            sb.append(indent()).append("</Else>");
            newLine(sb);
        }

        endScope();
        sb.append(indent()).append("</IfStatement>");

        return sb.toString();
    }

    @Override
    public String processExpressionStatement(AurExpressionStatement statement) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent()).append("<Expression>");
        newLine(sb);
        beginScope();

        sb.append(format(statement.expression));

        endScope();
        sb.append(indent()).append("</Expression>");
        newLine(sb);

        return sb.toString();
    }

    @Override
    public String processBodyStatement(AurBodyStatement statement) {
        StringBuilder sb = new StringBuilder();

        sb.append(indent()).append("<Body>");
        newLine(sb);
        beginScope();

        for (AurStatementNode statementNode : statement.statements) {
            sb.append(format(statementNode));
        }

        endScope();
        newLine(sb);
        sb.append(indent()).append("</Body>");
        newLine(sb);

        return sb.toString();
    }
}
