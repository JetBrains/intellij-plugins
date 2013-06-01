// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.jetbrains.lang.dart.DartTokenTypes.*;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartPsiImplUtil;

public class DartFunctionExpressionImpl extends AbstractDartComponentImpl implements DartFunctionExpression {

  public DartFunctionExpressionImpl(ASTNode node) {
    super(node);
  }

  @Override
  @Nullable
  public DartComponentName getComponentName() {
    return findChildByClass(DartComponentName.class);
  }

  @Override
  @NotNull
  public DartFormalParameterList getFormalParameterList() {
    return findNotNullChildByClass(DartFormalParameterList.class);
  }

  @Override
  @NotNull
  public DartFunctionExpressionBody getFunctionExpressionBody() {
    return findNotNullChildByClass(DartFunctionExpressionBody.class);
  }

  @Override
  @Nullable
  public DartReturnType getReturnType() {
    return findChildByClass(DartReturnType.class);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DartVisitor) ((DartVisitor)visitor).visitFunctionExpression(this);
    else super.accept(visitor);
  }

}
