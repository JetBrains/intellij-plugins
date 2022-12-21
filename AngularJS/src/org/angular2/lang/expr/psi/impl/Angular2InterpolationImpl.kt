// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl;

import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import org.angular2.lang.expr.psi.Angular2ElementVisitor;
import org.angular2.lang.expr.psi.Angular2Interpolation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2InterpolationImpl extends Angular2EmbeddedExpressionImpl implements Angular2Interpolation {

  public Angular2InterpolationImpl(IElementType elementType) {
    super(elementType);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Angular2ElementVisitor) {
      ((Angular2ElementVisitor)visitor).visitAngular2Interpolation(this);
    }
    else {
      super.accept(visitor);
    }
  }

  @Override
  public @Nullable JSExpression getExpression() {
    return Angular2BindingImpl.getExpression(this);
  }
}
