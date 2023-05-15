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

public class HCLPropertyImpl extends HCLPropertyMixin implements HCLProperty {

  public HCLPropertyImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HCLElementVisitor visitor) {
    visitor.visitProperty(this);
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
  public @NotNull HCLExpression getNameElement() {
    return HCLPsiImplUtilJ.getNameElement(this);
  }

  @Override
  public @Nullable HCLExpression getValue() {
    return HCLPsiImplUtilJ.getValue(this);
  }

  @Override
  public @Nullable ItemPresentation getPresentation() {
    return HCLPsiImplUtilJ.getPresentation(this);
  }

}
