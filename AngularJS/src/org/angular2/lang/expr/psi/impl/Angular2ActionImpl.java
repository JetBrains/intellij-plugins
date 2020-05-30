// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl;

import com.intellij.lang.javascript.psi.JSExpressionStatement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import org.angular2.lang.expr.psi.Angular2Action;
import org.angular2.lang.expr.psi.Angular2Chain;
import org.angular2.lang.expr.psi.Angular2ElementVisitor;
import org.jetbrains.annotations.NotNull;

public class Angular2ActionImpl extends Angular2EmbeddedExpressionImpl implements Angular2Action {

  public Angular2ActionImpl(IElementType elementType) {
    super(elementType);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Angular2ElementVisitor) {
      ((Angular2ElementVisitor)visitor).visitAngular2Action(this);
    }
    else {
      super.accept(visitor);
    }
  }

  @Override
  public JSExpressionStatement @NotNull [] getStatements() {
    for (PsiElement child : getChildren()) {
      if (child instanceof Angular2Chain) {
        return ((Angular2Chain)child).getStatements();
      }
      if (child instanceof JSExpressionStatement) {
        return new JSExpressionStatement[]{(JSExpressionStatement)child};
      }
    }
    return new JSExpressionStatement[0];
  }
}
