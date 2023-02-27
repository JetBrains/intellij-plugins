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

public class HCLArrayImpl extends HCLContainerImpl implements HCLArray {

  public HCLArrayImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull HCLElementVisitor visitor) {
    visitor.visitArray(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HCLElementVisitor) accept((HCLElementVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  public @Nullable ItemPresentation getPresentation() {
    return HCLPsiImplUtilJ.getPresentation(this);
  }

  @Override
  @NotNull
  public List<HCLExpression> getElements() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HCLExpression.class);
  }

}
