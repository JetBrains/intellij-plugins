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

public class MakefileVariableAssignmentImpl extends ASTWrapperPsiElement implements MakefileVariableAssignment {

  public MakefileVariableAssignmentImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MakefileVisitor visitor) {
    visitor.visitVariableAssignment(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MakefileVisitor) accept((MakefileVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public MakefileVariable getVariable() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, MakefileVariable.class));
  }

  @Override
  @Nullable
  public MakefileVariableValue getVariableValue() {
    return PsiTreeUtil.getChildOfType(this, MakefileVariableValue.class);
  }

  @Override
  @Nullable
  public PsiElement getAssignment() {
    return MakefilePsiImplUtil.getAssignment(this);
  }

  @Override
  @Nullable
  public String getValue() {
    return MakefilePsiImplUtil.getValue(this);
  }

}
