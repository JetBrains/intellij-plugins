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
import com.intellij.navigation.ItemPresentation;

public class JdlRelationshipGroupImpl extends ASTWrapperPsiElement implements JdlRelationshipGroup {

  public JdlRelationshipGroupImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull JdlVisitor visitor) {
    visitor.visitRelationshipGroup(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof JdlVisitor) accept((JdlVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<JdlRelationshipMapping> getRelationshipMappingList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, JdlRelationshipMapping.class);
  }

  @Override
  @Nullable
  public JdlRelationshipType getRelationshipType() {
    return findChildByClass(JdlRelationshipType.class);
  }

  @Override
  public @NotNull String getType() {
    return JdlPsiUtils.getType(this);
  }

  @Override
  public @NotNull ItemPresentation getPresentation() {
    return JdlPsiUtils.getPresentation(this);
  }

}
