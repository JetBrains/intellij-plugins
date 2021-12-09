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

public class DartSwitchStatementImpl extends DartPsiCompositeElementImpl implements DartSwitchStatement {

  public DartSwitchStatementImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull DartVisitor visitor) {
    visitor.visitSwitchStatement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DartVisitor) accept((DartVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public DartDefaultCase getDefaultCase() {
    return findChildByClass(DartDefaultCase.class);
  }

  @Override
  @Nullable
  public DartExpression getExpression() {
    return findChildByClass(DartExpression.class);
  }

  @Override
  @NotNull
  public List<DartSwitchCase> getSwitchCaseList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartSwitchCase.class);
  }

}
