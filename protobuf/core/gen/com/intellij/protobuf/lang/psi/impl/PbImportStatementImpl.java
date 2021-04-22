// This is a generated file. Not intended for manual editing.
package com.intellij.protobuf.lang.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.intellij.protobuf.lang.psi.PbTypes.*;
import com.intellij.protobuf.lang.psi.*;
import com.intellij.protobuf.lang.psi.util.PbPsiImplUtil;

public class PbImportStatementImpl extends PbImportStatementMixin implements PbImportStatement {

  public PbImportStatementImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PbVisitor visitor) {
    visitor.visitImportStatement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PbVisitor) accept((PbVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PbImportName getImportName() {
    return PsiTreeUtil.getChildOfType(this, PbImportName.class);
  }

}
