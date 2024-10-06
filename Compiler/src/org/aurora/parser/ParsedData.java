package org.aurora.parser;

import org.aurora.component.AurIOComponent;
import org.aurora.parser.expression.AurExpressionNode;

import java.util.List;

public class ParsedData extends AurIOComponent {

    private final List<AurExpressionNode> expressions;

    public ParsedData(List<AurExpressionNode> expressions) {
        this.expressions = expressions;
    }

    public List<AurExpressionNode> getExpressions() {
        return expressions;
    }

    @Override
    public AurIOComponent clone() {
        return new ParsedData(expressions);
    }
}
