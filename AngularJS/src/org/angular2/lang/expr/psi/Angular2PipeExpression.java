// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi;

import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public interface Angular2PipeExpression extends JSCallExpression {

  @Override
  @Nullable
  String getName();

  static boolean isPipeNameReference(JSReferenceExpression referenceExpression) {
    PsiElement parent = referenceExpression.getParent();
    return parent instanceof Angular2PipeExpression
           && referenceExpression.equals(((Angular2PipeExpression)parent).getMethodExpression());
  }
}
