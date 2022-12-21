// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl;

import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.IncorrectOperationException;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.lang.expr.psi.Angular2PipeReferenceExpression;
import org.jetbrains.annotations.NotNull;

public class Angular2PipeReferenceExpressionImpl extends JSReferenceExpressionImpl implements Angular2PipeReferenceExpression {

  public Angular2PipeReferenceExpressionImpl(IElementType elementType) {
    super(elementType);
  }

  @Override
  public boolean isReferenceTo(@NotNull PsiElement element) {
    if (Angular2EntitiesProvider.isPipeTransformMethod(element)) {
      return element.equals(resolve());
    }
    return false;
  }

  @Override
  public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
    return handleElementRenameInternal(newElementName);
  }
}
