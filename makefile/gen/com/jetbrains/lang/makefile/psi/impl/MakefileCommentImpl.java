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

public class MakefileCommentImpl extends ASTWrapperPsiElement implements MakefileComment {

  public MakefileCommentImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MakefileVisitor visitor) {
    visitor.visitComment(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MakefileVisitor) accept((MakefileVisitor)visitor);
    else super.accept(visitor);
  }

}
