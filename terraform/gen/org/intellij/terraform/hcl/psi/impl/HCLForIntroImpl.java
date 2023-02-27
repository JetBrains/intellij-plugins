// This is a generated file. Not intended for manual editing.
package org.intellij.terraform.hcl.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static org.intellij.terraform.hcl.HCLElementTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import org.intellij.terraform.hcl.psi.*;

public class HCLForIntroImpl extends ASTWrapperPsiElement implements HCLForIntro {

  public HCLForIntroImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HCLElementVisitor visitor) {
    visitor.visitForIntro(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HCLElementVisitor) accept((HCLElementVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  public @Nullable HCLIdentifier getVar1() {
    return HCLPsiImplUtilJ.getVar1(this);
  }

  @Override
  public @Nullable HCLIdentifier getVar2() {
    return HCLPsiImplUtilJ.getVar2(this);
  }

  @Override
  public @Nullable HCLExpression getContainer() {
    return HCLPsiImplUtilJ.getContainer(this);
  }

}
