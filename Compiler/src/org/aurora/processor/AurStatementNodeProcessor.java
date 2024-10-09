package org.aurora.processor;

import org.aurora.parser.statement.AurBodyStatement;
import org.aurora.parser.statement.AurExpressionStatement;
import org.aurora.parser.statement.AurIfStatement;
import org.aurora.parser.statement.PrintStatement;

public interface AurStatementNodeProcessor<T> {

    T processIfStatement(AurIfStatement statement);

    T processExpressionStatement(AurExpressionStatement statement);

    T processBodyStatement(AurBodyStatement statement);

    T processPrintStatement(PrintStatement statement);
}
