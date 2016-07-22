package org.intellij.plugins.postcss.psi.impl;

import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.impl.source.tree.CompositePsiElement;
import org.intellij.plugins.postcss.PostCssElementTypes;
import org.intellij.plugins.postcss.psi.PostCssCustomSelector;
import org.jetbrains.annotations.NotNull;

public class PostCssCustomSelectorImpl extends CompositePsiElement implements PostCssCustomSelector {
  public PostCssCustomSelectorImpl() {
    super(PostCssElementTypes.POST_CSS_CUSTOM_SELECTOR);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PostCssElementVisitor) {
      ((PostCssElementVisitor)visitor).visitPostCssCustomSelector(this);
    }
    else {
      visitor.visitElement(this);
    }
  }
}