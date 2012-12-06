package com.jetbrains.lang.dart.ide.surroundWith.statement;

import com.jetbrains.lang.dart.psi.DartLiteralExpression;

/**
 * @author: Fedor.Korotkov
 */
public abstract class DartLiteralAndBlockStatementSurrounderBase extends DartBlockAndChildStatementSurrounderBase<DartLiteralExpression> {
  @Override
  protected Class<DartLiteralExpression> getClassToDelete() {
    return DartLiteralExpression.class;
  }
}
