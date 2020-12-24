// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.makefile.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.jetbrains.lang.makefile.psi.MakefileTypes.*;
import com.jetbrains.lang.makefile.psi.*;

public class MakefileVariableImpl extends MakefileVariableNamedElementImpl implements MakefileVariable {

  public MakefileVariableImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MakefileVisitor visitor) {
    visitor.visitVariable(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MakefileVisitor) accept((MakefileVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public MakefileIdentifier getIdentifier() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, MakefileIdentifier.class));
  }

  @Override
  @NotNull
  public String getName() {
    return MakefilePsiImplUtil.getName(this);
  }

  @Override
  @NotNull
  public PsiElement setName(@NotNull String newName) {
    return MakefilePsiImplUtil.setName(this, newName);
  }

  @Override
  @Nullable
  public PsiElement getNameIdentifier() {
    return MakefilePsiImplUtil.getNameIdentifier(this);
  }

}
