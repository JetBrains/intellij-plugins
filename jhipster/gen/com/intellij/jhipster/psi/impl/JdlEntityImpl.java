// This is a generated file. Not intended for manual editing.
package com.intellij.jhipster.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.intellij.jhipster.psi.JdlTokenTypes.*;
import com.intellij.jhipster.psi.JdlEntityMixin;
import com.intellij.jhipster.psi.*;

public class JdlEntityImpl extends JdlEntityMixin implements JdlEntity {

  public JdlEntityImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull JdlVisitor visitor) {
    visitor.visitEntity(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof JdlVisitor) accept((JdlVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<JdlAnnotation> getAnnotationList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, JdlAnnotation.class);
  }

  @Override
  @NotNull
  public List<JdlEntityFieldMapping> getEntityFieldMappingList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, JdlEntityFieldMapping.class);
  }

  @Override
  @Nullable
  public JdlEntityId getEntityId() {
    return findChildByClass(JdlEntityId.class);
  }

  @Override
  @Nullable
  public JdlEntityTableName getEntityTableName() {
    return findChildByClass(JdlEntityTableName.class);
  }

}
