// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl;

import com.intellij.lang.Language;
import com.intellij.lang.javascript.psi.JSSuppressionHolder;
import com.intellij.lang.javascript.psi.impl.JSElementImpl;
import com.intellij.psi.tree.IElementType;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.expr.psi.Angular2EmbeddedExpression;
import org.jetbrains.annotations.NotNull;

public class Angular2EmbeddedExpressionImpl extends JSElementImpl implements JSSuppressionHolder, Angular2EmbeddedExpression {

  public Angular2EmbeddedExpressionImpl(IElementType elementType) {
    super(elementType);
  }

  @NotNull
  @Override
  public Language getLanguage() {
    return Angular2Language.INSTANCE;
  }

}
