// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.surroundWith.statement;

import com.jetbrains.lang.dart.psi.DartLiteralExpression;

public abstract class DartLiteralAndBlockStatementSurrounderBase extends DartBlockAndChildStatementSurrounderBase<DartLiteralExpression> {
  @Override
  protected Class<DartLiteralExpression> getClassToDelete() {
    return DartLiteralExpression.class;
  }
}
