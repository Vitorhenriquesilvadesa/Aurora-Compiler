package org.aurora.interceptor;

import org.aurora.parser.ParsedData;
import org.aurora.parser.expression.*;
import org.aurora.processor.AurExpressionNodeProcessor;
import org.aurora.scanner.ScannedData;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class AurParsedASTPrinterInterceptor implements AurPassiveInterceptor<ScannedData, ParsedData>, AurExpressionNodeProcessor<String> {

    private int depth = 0;
    private final int tabSize = 2;

    @Override
    public void beforeState(ScannedData input) {

    }

    @Override
    public void afterState(ParsedData input) {

        try (FileWriter writer = new FileWriter("/home/vitor/IdeaProjects/Aurora/res/test.xml")) {
            PrintWriter printer = new PrintWriter(writer);

            printer.println("<Program>");
            System.out.println("<Program>");
            beginScope();

            for (AurExpressionNode expressionNode : input.getExpressions()) {
                String formatedExpression = format(expressionNode);
                System.out.println(formatedExpression);
                printer.println(formatedExpression);
            }

            printer.print("</Program>");
            System.out.print("</Program>");
            endScope();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String format(AurExpressionNode expression) {
        return expression.acceptProcessor(this);
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
}
