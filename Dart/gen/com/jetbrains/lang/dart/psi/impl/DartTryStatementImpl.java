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

public class DartTryStatementImpl extends DartPsiCompositeElementImpl implements DartTryStatement {

  public DartTryStatementImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull DartVisitor visitor) {
    visitor.visitTryStatement(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DartVisitor) accept((DartVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public DartBlock getBlock() {
    return findChildByClass(DartBlock.class);
  }

  @Override
  @Nullable
  public DartFinallyPart getFinallyPart() {
    return findChildByClass(DartFinallyPart.class);
  }

  @Override
  @NotNull
  public List<DartOnPart> getOnPartList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartOnPart.class);
  }

}
