package org.aurora.processor;

import org.aurora.parser.expression.AurBinaryExpression;
import org.aurora.parser.expression.AurLiteralExpression;
import org.aurora.parser.expression.AurLogicalExpression;
import org.aurora.parser.expression.AurUnaryExpression;

public interface AurExpressionNodeProcessor<T> {

    T processLiteralExpression(AurLiteralExpression expression);

    T processBinaryExpression(AurBinaryExpression expression);

    T processUnaryExpression(AurUnaryExpression expression);

    T processLogicalExpression(AurLogicalExpression expression);
}
