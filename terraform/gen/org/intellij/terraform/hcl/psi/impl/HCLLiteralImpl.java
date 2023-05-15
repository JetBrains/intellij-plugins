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

public abstract class HCLLiteralImpl extends HCLLiteralMixin implements HCLLiteral {

  public HCLLiteralImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HCLElementVisitor visitor) {
    visitor.visitLiteral(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HCLElementVisitor) accept((HCLElementVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  public boolean isQuotedString() {
    return HCLPsiImplUtilJ.isQuotedString(this);
  }

}
