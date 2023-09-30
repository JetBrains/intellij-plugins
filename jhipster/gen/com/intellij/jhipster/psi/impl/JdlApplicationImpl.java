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

public class JdlApplicationImpl extends ASTWrapperPsiElement implements JdlApplication {

  public JdlApplicationImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull JdlVisitor visitor) {
    visitor.visitApplication(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof JdlVisitor) accept((JdlVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<JdlConfigBlock> getConfigBlockList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, JdlConfigBlock.class);
  }

  @Override
  @NotNull
  public List<JdlConfigurationOption> getConfigurationOptionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, JdlConfigurationOption.class);
  }

  @Override
  @NotNull
  public List<JdlUseConfigurationOption> getUseConfigurationOptionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, JdlUseConfigurationOption.class);
  }

  @Override
  public @NotNull String getName() {
    return JdlPsiUtils.getName(this);
  }

  @Override
  public @NotNull ItemPresentation getPresentation() {
    return JdlPsiUtils.getPresentation(this);
  }

}
