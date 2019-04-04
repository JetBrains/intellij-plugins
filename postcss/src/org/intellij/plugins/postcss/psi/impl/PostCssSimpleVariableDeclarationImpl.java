package org.intellij.plugins.postcss.psi.impl;

import com.intellij.css.util.CssPsiUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.css.impl.util.CssUtil;
import com.intellij.psi.impl.source.tree.CompositePsiElement;
import com.intellij.util.IncorrectOperationException;
import org.intellij.plugins.postcss.PostCssElementTypes;
import org.intellij.plugins.postcss.psi.PostCssSimpleVariableDeclaration;
import org.jetbrains.annotations.NotNull;

public class PostCssSimpleVariableDeclarationImpl extends CompositePsiElement implements PostCssSimpleVariableDeclaration {
  public PostCssSimpleVariableDeclarationImpl() {
    super(PostCssElementTypes.POST_CSS_SIMPLE_VARIABLE_DECLARATION);
  }

  @NotNull
  @Override
  public String getName() {
    return StringUtil.trimLeading(getNameIdentifier().getText(), '$');
  }

  @NotNull
  @Override
  public PsiElement getNameIdentifier() {
    return getFirstChild();
  }

  @Override
  public int getLineNumber() {
    return CssUtil.getLineNumber(this);
  }

  @NotNull
  @Override
  public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
    CssPsiUtil.replaceToken(getNameIdentifier(), name);
    return this;
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PostCssElementVisitor) {
      ((PostCssElementVisitor)visitor).visitPostCssSimpleVariableDeclaration(this);
    }
    else {
      super.accept(visitor);
    }
  }
}
