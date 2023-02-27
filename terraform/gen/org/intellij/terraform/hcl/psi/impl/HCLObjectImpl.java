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

public class HCLObjectImpl extends HCLContainerImpl implements HCLObject {

  public HCLObjectImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull HCLElementVisitor visitor) {
    visitor.visitObject(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HCLElementVisitor) accept((HCLElementVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<HCLProperty> getPropertyList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HCLProperty.class);
  }

  @Override
  public @Nullable HCLProperty findProperty(@NotNull String name) {
    return HCLPsiImplUtilJ.findProperty(this, name);
  }

  @Override
  public @Nullable ItemPresentation getPresentation() {
    return HCLPsiImplUtilJ.getPresentation(this);
  }

  @Override
  public @NotNull List<HCLBlock> getBlockList() {
    return HCLPsiImplUtilJ.getBlockList(this);
  }

  @Override
  public @NotNull List<HCLExpression> getElements() {
    return HCLPsiImplUtilJ.getElements(this);
  }

}
