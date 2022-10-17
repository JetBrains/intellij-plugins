// This is a generated file. Not intended for manual editing.
package com.intellij.plugins.drools.lang.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.*;
import com.intellij.plugins.drools.lang.psi.*;

public class DroolsPrimaryExprImpl extends DroolsPrimaryExprVarImpl implements DroolsPrimaryExpr {

  public DroolsPrimaryExprImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull DroolsVisitor visitor) {
    visitor.visitPrimaryExpr(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DroolsVisitor) accept((DroolsVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public DroolsArguments getArguments() {
    return findChildByClass(DroolsArguments.class);
  }

  @Override
  @Nullable
  public DroolsCreator getCreator() {
    return findChildByClass(DroolsCreator.class);
  }

  @Override
  @Nullable
  public DroolsExplicitGenericInvocationSuffix getExplicitGenericInvocationSuffix() {
    return findChildByClass(DroolsExplicitGenericInvocationSuffix.class);
  }

  @Override
  @NotNull
  public List<DroolsExpression> getExpressionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DroolsExpression.class);
  }

  @Override
  @NotNull
  public List<DroolsIdentifier> getIdentifierList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DroolsIdentifier.class);
  }

  @Override
  @NotNull
  public List<DroolsIdentifierSuffix> getIdentifierSuffixList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DroolsIdentifierSuffix.class);
  }

  @Override
  @Nullable
  public DroolsMapExpressionList getMapExpressionList() {
    return findChildByClass(DroolsMapExpressionList.class);
  }

  @Override
  @Nullable
  public DroolsNonWildcardTypeArguments getNonWildcardTypeArguments() {
    return findChildByClass(DroolsNonWildcardTypeArguments.class);
  }

  @Override
  @Nullable
  public DroolsPrimitiveType getPrimitiveType() {
    return findChildByClass(DroolsPrimitiveType.class);
  }

  @Override
  @Nullable
  public DroolsSuperSuffix getSuperSuffix() {
    return findChildByClass(DroolsSuperSuffix.class);
  }

}
