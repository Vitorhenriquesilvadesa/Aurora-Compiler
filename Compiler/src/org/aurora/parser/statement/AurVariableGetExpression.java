package org.aurora.parser.statement;

import org.aurora.parser.expression.AurExpressionNode;
import org.aurora.processor.AurExpressionNodeProcessor;
import org.aurora.scanner.Token;

public class AurVariableGetExpression extends AurExpressionNode {

    public final Token name;

    public AurVariableGetExpression(Token name) {
        this.name = name;
    }

    @Override
    public <T> T acceptProcessor(AurExpressionNodeProcessor<T> processor) {
        return processor.processVariableGetExpression(this);
    }
}