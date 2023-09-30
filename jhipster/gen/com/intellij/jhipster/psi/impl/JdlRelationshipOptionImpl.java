// This is a generated file. Not intended for manual editing.
package com.intellij.jhipster.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.intellij.jhipster.psi.JdlTokenTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.jhipster.psi.*;

public class JdlRelationshipOptionImpl extends ASTWrapperPsiElement implements JdlRelationshipOption {

  public JdlRelationshipOptionImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull JdlVisitor visitor) {
    visitor.visitRelationshipOption(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof JdlVisitor) accept((JdlVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public JdlRelationshipOptionId getRelationshipOptionId() {
    return findChildByClass(JdlRelationshipOptionId.class);
  }

  @Override
  @Nullable
  public JdlStringLiteral getStringLiteral() {
    return findChildByClass(JdlStringLiteral.class);
  }

}
