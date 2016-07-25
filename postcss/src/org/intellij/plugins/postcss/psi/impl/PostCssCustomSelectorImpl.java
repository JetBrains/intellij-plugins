package org.intellij.plugins.postcss.psi.impl;

import com.intellij.css.util.CssPsiUtil;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.css.impl.stubs.base.CssNamedStubElement;
import com.intellij.psi.css.impl.stubs.base.CssNamedStubElementType;
import com.intellij.util.IncorrectOperationException;
import org.intellij.plugins.postcss.psi.PostCssCustomSelector;
import org.intellij.plugins.postcss.psi.stubs.impl.PostCssCustomSelectorStub;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PostCssCustomSelectorImpl extends CssNamedStubElement<PostCssCustomSelectorStub> implements PostCssCustomSelector {

  public PostCssCustomSelectorImpl(@NotNull PostCssCustomSelectorStub stub, @NotNull CssNamedStubElementType nodeType) {
    super(stub, nodeType);
  }

  public PostCssCustomSelectorImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Nullable
  @Override
  public PsiElement getNameIdentifier() {
    PsiElement token = getLastChild();
    if (token == null || token.getNode().getElementType() != CssElementTypes.CSS_IDENT) return null;
    return token;
  }

  @Override
  @NotNull
  public String getName() {
    PostCssCustomSelectorStub stub = getStub();
    if (stub != null) {
      return stub.getName();
    }

    PsiElement nameIdentifier = getNameIdentifier();
    return nameIdentifier != null ? nameIdentifier.getText() : "";
  }

  @Override
  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    PsiElement nameIdentifier = getNameIdentifier();
    if (nameIdentifier != null) {
      CssPsiUtil.replaceToken(nameIdentifier, name);
    }
    return this;
  }

  @Override
  public int getTextOffset() {
    PsiElement identifier = getNameIdentifier();
    return identifier != null ? identifier.getTextOffset() : super.getTextOffset();
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