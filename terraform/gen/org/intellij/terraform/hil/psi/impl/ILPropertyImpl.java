// This is a generated file. Not intended for manual editing.
package org.intellij.terraform.hil.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static org.intellij.terraform.hil.HILElementTypes.*;
import org.intellij.terraform.hil.psi.*;

public class ILPropertyImpl extends ILPropertyMixin implements ILProperty {

  public ILPropertyImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull ILGeneratedVisitor visitor) {
    visitor.visitILProperty(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ILGeneratedVisitor) accept((ILGeneratedVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  public @NotNull String getName() {
    return HILPsiImplUtilJ.getName(this);
  }

  @Override
  public @NotNull ILExpression getNameElement() {
    return HILPsiImplUtilJ.getNameElement(this);
  }

  @Override
  public @Nullable ILExpression getValue() {
    return HILPsiImplUtilJ.getValue(this);
  }

}
