// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.model.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CfmlLiteralExpressionType extends CfmlCompositeElementType {
  private final String myTypeName;
  private final PsiPrimitiveType myPrimitiveType;

  public CfmlLiteralExpressionType(@NotNull @NonNls String debugName, @NotNull String typeName) {
    super(debugName);
    myTypeName = typeName;
    myPrimitiveType = null;
  }

  public CfmlLiteralExpressionType(@NotNull @NonNls String debugName, @NotNull PsiType primitiveType) {
    super(debugName);
    myTypeName = null;
    assert primitiveType instanceof PsiPrimitiveType;
    myPrimitiveType = (PsiPrimitiveType)primitiveType;
  }

  @Override
  public PsiElement createPsiElement(ASTNode node) {
    return new CfmlLiteralExpression(node);
  }

  class CfmlLiteralExpression extends CfmlCompositeElement implements CfmlExpression {
    CfmlLiteralExpression(final @NotNull ASTNode node) {
      super(node);
    }

    @Override
    public @Nullable PsiType getPsiType() {
      if (myPrimitiveType != null) {
        return myPrimitiveType;
      }
      return CfmlPsiUtil.getTypeByName(myTypeName, getProject());
    }
  }
}
