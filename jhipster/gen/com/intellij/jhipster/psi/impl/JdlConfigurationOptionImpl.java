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

public class JdlConfigurationOptionImpl extends ASTWrapperPsiElement implements JdlConfigurationOption {

  public JdlConfigurationOptionImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull JdlVisitor visitor) {
    visitor.visitConfigurationOption(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof JdlVisitor) accept((JdlVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public JdlConfigurationOptionName getConfigurationOptionName() {
    return findNotNullChildByClass(JdlConfigurationOptionName.class);
  }

  @Override
  @Nullable
  public JdlEntitiesList getEntitiesList() {
    return findChildByClass(JdlEntitiesList.class);
  }

  @Override
  @Nullable
  public JdlExceptEntities getExceptEntities() {
    return findChildByClass(JdlExceptEntities.class);
  }

  @Override
  @Nullable
  public JdlWildcardLiteral getWildcardLiteral() {
    return findChildByClass(JdlWildcardLiteral.class);
  }

  @Override
  @Nullable
  public JdlWithOptionValue getWithOptionValue() {
    return findChildByClass(JdlWithOptionValue.class);
  }

  @Override
  public @NotNull String getName() {
    return JdlPsiUtils.getName(this);
  }

  @Override
  public @NotNull JdlConfigurationOptionName getNameElement() {
    return JdlPsiUtils.getNameElement(this);
  }

  @Override
  public @NotNull ItemPresentation getPresentation() {
    return JdlPsiUtils.getPresentation(this);
  }

}
