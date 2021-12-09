// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.makefile.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.jetbrains.lang.makefile.psi.MakefileTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.jetbrains.lang.makefile.psi.*;

public class MakefileBlockImpl extends ASTWrapperPsiElement implements MakefileBlock {

  public MakefileBlockImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MakefileVisitor visitor) {
    visitor.visitBlock(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MakefileVisitor) accept((MakefileVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<MakefileCommand> getCommandList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, MakefileCommand.class);
  }

  @Override
  @NotNull
  public List<MakefileConditional> getConditionalList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, MakefileConditional.class);
  }

  @Override
  @NotNull
  public List<MakefileDirective> getDirectiveList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, MakefileDirective.class);
  }

  @Override
  @NotNull
  public List<MakefileFunction> getFunctionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, MakefileFunction.class);
  }

  @Override
  @NotNull
  public List<MakefileRule> getRuleList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, MakefileRule.class);
  }

  @Override
  @NotNull
  public List<MakefileVariableAssignment> getVariableAssignmentList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, MakefileVariableAssignment.class);
  }

}
