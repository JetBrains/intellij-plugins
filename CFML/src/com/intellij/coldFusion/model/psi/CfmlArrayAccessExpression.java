package com.intellij.coldFusion.model.psi;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class CfmlArrayAccessExpression extends CfmlCompositeElement implements CfmlExpression, CfmlTypedElement {
  public CfmlArrayAccessExpression(@NotNull ASTNode node) {
    super(node);
  }

  @Nullable
  public CfmlReference getReferenceExpression() {
    return findChildByClass(CfmlReferenceExpression.class);
  }

  @Nullable
  public PsiType getExternalType() {
    final CfmlReference referenceExpression = getReferenceExpression();
    if (referenceExpression != null) {
      final PsiElement resolve = referenceExpression.resolve();
      return resolve instanceof CfmlVariable ? ((CfmlVariable)resolve).getPsiType() : null;
    }
    return null;
  }

  @Override
  @Nullable
  public PsiType getPsiType() {
    PsiType type = getExternalType();
    
    if (type == null) {
      CfmlReference referenceExpression = getReferenceExpression();
      type = referenceExpression != null ? referenceExpression.getPsiType() : null;
    }

    if (type instanceof CfmlArrayType) {
      return ((CfmlArrayType)type).getComponentType();
    }
    return null;
  }
}
