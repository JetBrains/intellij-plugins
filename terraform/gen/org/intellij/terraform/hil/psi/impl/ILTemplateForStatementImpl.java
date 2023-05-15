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

public class ILTemplateForStatementImpl extends ILTemplateStatementImpl implements ILTemplateForStatement {

  public ILTemplateForStatementImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull ILGeneratedVisitor visitor) {
    visitor.visitILTemplateForStatement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ILGeneratedVisitor) accept((ILGeneratedVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  public @NotNull ILVariable getVar1() {
    return HILPsiImplUtilJ.getVar1(this);
  }

  @Override
  public @Nullable ILVariable getVar2() {
    return HILPsiImplUtilJ.getVar2(this);
  }

  @Override
  public @NotNull ILExpression getContainer() {
    return HILPsiImplUtilJ.getContainer(this);
  }

}
