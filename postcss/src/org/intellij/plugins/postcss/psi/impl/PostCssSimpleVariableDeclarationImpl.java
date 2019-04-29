package org.intellij.plugins.postcss.psi.impl;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.css.CssTermList;
import com.intellij.psi.css.impl.util.CssUtil;
import com.intellij.psi.impl.source.tree.CompositePsiElement;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.intellij.plugins.postcss.PostCssElementTypes;
import org.intellij.plugins.postcss.PostCssLanguage;
import org.intellij.plugins.postcss.lexer.PostCssTokenTypes;
import org.intellij.plugins.postcss.psi.PostCssSimpleVariableDeclaration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

  @Nullable
  @Override
  public CssTermList getInitializer() {
    return PsiTreeUtil.getChildOfType(this, CssTermList.class);
  }

  @Override
  public int getLineNumber() {
    return CssUtil.getLineNumber(this);
  }

  @NotNull
  @Override
  public SearchScope getUseScope() {
    return new LocalSearchScope(getContainingFile());
  }

  @NotNull
  @Override
  public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
    PsiFile file = PsiFileFactory.getInstance(getProject()).createFileFromText(PostCssLanguage.INSTANCE, "$" + name);
    PsiElement oldVarToken = getNameIdentifier().getFirstChild();
    PsiElement newVarToken = PsiTreeUtil.getDeepestFirst(file);
    if (oldVarToken != null &&
        oldVarToken.getNode().getElementType() == PostCssTokenTypes.POST_CSS_SIMPLE_VARIABLE_TOKEN &&
        newVarToken.getNode().getElementType() == PostCssTokenTypes.POST_CSS_SIMPLE_VARIABLE_TOKEN) {
      oldVarToken.replace(newVarToken);
    }
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
