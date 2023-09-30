// This is a generated file. Not intended for manual editing.
package com.intellij.jhipster.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.intellij.jhipster.psi.JdlTokenTypes.*;
import com.intellij.jhipster.psi.JdlConstantMixin;
import com.intellij.jhipster.psi.*;

public class JdlConstantImpl extends JdlConstantMixin implements JdlConstant {

  public JdlConstantImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull JdlVisitor visitor) {
    visitor.visitConstant(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof JdlVisitor) accept((JdlVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public JdlConstantName getConstantName() {
    return findNotNullChildByClass(JdlConstantName.class);
  }

  @Override
  @Nullable
  public JdlValue getValue() {
    return findChildByClass(JdlValue.class);
  }

}
