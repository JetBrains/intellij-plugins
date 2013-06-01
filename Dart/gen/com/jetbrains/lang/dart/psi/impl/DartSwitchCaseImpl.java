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

public class DartSwitchCaseImpl extends DartPsiCompositeElementImpl implements DartSwitchCase {

  public DartSwitchCaseImpl(ASTNode node) {
    super(node);
  }

  @Override
  @NotNull
  public List<DartExpression> getExpressionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartExpression.class);
  }

  @Override
  @Nullable
  public DartLabel getLabel() {
    return findChildByClass(DartLabel.class);
  }

  @Override
  @NotNull
  public DartStatements getStatements() {
    return findNotNullChildByClass(DartStatements.class);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DartVisitor) ((DartVisitor)visitor).visitSwitchCase(this);
    else super.accept(visitor);
  }

}
