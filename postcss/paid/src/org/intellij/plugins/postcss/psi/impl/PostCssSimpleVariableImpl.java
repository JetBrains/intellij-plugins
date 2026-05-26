package org.intellij.plugins.postcss.psi.impl;

import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.tree.CompositePsiElement;
import org.intellij.plugins.postcss.PostCssElementTypes;
import org.intellij.plugins.postcss.psi.PostCssSimpleVariable;
import org.intellij.plugins.postcss.psi.PostCssSimpleVariableDeclaration;
import org.intellij.plugins.postcss.references.PostCssSimpleVariableReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PostCssSimpleVariableImpl extends CompositePsiElement implements PostCssSimpleVariable {
  public PostCssSimpleVariableImpl() {
    super(PostCssElementTypes.POST_CSS_SIMPLE_VARIABLE);
  }

  @Override
  public @Nullable PsiReference getReference() {
    if (getParent() instanceof PostCssSimpleVariableDeclaration) {
      return null;
    }
    return new PostCssSimpleVariableReference(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PostCssElementVisitor) {
      ((PostCssElementVisitor)visitor).visitPostCssSimpleVariable(this);
    }
    else {
      super.accept(visitor);
    }
  }
}
