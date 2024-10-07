package org.aurora.parser;

import org.aurora.component.AurIOComponent;
import org.aurora.parser.statement.AurStatementNode;

import java.util.List;

public class ParsedData extends AurIOComponent {

    private final List<AurStatementNode> expressions;

    public ParsedData(List<AurStatementNode> expressions) {
        this.expressions = expressions;
    }

    public List<AurStatementNode> getExpressions() {
        return expressions;
    }

    @Override
    public AurIOComponent clone() {
        return new ParsedData(expressions);
    }
}
