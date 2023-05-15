// This is a generated file. Not intended for manual editing.
package org.intellij.terraform.hcl.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static org.intellij.terraform.hcl.HCLElementTypes.*;
import org.intellij.terraform.hcl.psi.*;
import com.intellij.navigation.ItemPresentation;

public class HCLBlockImpl extends HCLBlockMixin implements HCLBlock {

  public HCLBlockImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HCLElementVisitor visitor) {
    visitor.visitBlock(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HCLElementVisitor) accept((HCLElementVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  public @NotNull String getName() {
    return HCLPsiImplUtilJ.getName(this);
  }

  @Override
  public @NotNull String getFullName() {
    return HCLPsiImplUtilJ.getFullName(this);
  }

  @Override
  public HCLElement @NotNull [] getNameElements() {
    return HCLPsiImplUtilJ.getNameElements(this);
  }

  @Override
  public @Nullable HCLObject getObject() {
    return HCLPsiImplUtilJ.getObject(this);
  }

  @Override
  public @Nullable ItemPresentation getPresentation() {
    return HCLPsiImplUtilJ.getPresentation(this);
  }

}
