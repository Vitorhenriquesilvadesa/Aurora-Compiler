package org.aurora.processor;

import org.aurora.parser.statement.AurBodyStatement;
import org.aurora.parser.statement.AurExpressionStatement;
import org.aurora.parser.statement.AurIfStatement;

public interface AurStatementNodeProcessor<T> {

    T processIfStatement(AurIfStatement statement);

    T processExpressionStatement(AurExpressionStatement statement);

    T processBodyStatement(AurBodyStatement statement);
}
