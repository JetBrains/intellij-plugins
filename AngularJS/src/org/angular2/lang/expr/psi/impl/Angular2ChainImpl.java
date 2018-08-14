// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JSExtendedLanguagesTokenSetProvider;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.impl.JSStatementImpl;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import org.angular2.lang.expr.psi.Angular2Chain;
import org.angular2.lang.expr.psi.Angular2ElementVisitor;
import org.jetbrains.annotations.NotNull;

public class Angular2ChainImpl extends JSStatementImpl implements Angular2Chain {

  public Angular2ChainImpl(IElementType elementType) {
    super(elementType);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Angular2ElementVisitor) {
      ((Angular2ElementVisitor)visitor).visitAngular2Chain(this);
    }
    else {
      super.accept(visitor);
    }
  }

  @Override
  @NotNull
  public JSExpression[] getExpressions() {
    final ASTNode[] nodes = getChildren(JSExtendedLanguagesTokenSetProvider.EXPRESSIONS);
    if (nodes.length == 0) return JSExpression.EMPTY_ARRAY;
    final JSExpression[] exprs = new JSExpression[nodes.length];
    for (int i = 0; i < exprs.length; i++) {
      exprs[i] = nodes[i].getPsi(JSExpression.class);
    }
    return exprs;
  }
}
