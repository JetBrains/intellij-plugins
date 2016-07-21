package org.intellij.plugins.postcss.psi.stubs;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.impl.stubs.CssPseudoSelectorStub;
import com.intellij.psi.css.impl.stubs.CssPseudoSelectorStubElementType;
import org.intellij.plugins.postcss.psi.PostCssCustomSelector;
import org.intellij.plugins.postcss.psi.impl.PostCssCustomSelectorImpl;
import org.jetbrains.annotations.NotNull;

public class PostCssCustomSelectorStubElementType extends CssPseudoSelectorStubElementType<PostCssCustomSelector> {
  public PostCssCustomSelectorStubElementType() {
    super("POST_CSS_CUSTOM_SELECTOR");
  }

  @Override
  public PsiElement createElement(ASTNode node) {
    return new PostCssCustomSelectorImpl(node);
  }

  @Override
  public PostCssCustomSelector createPsi(@NotNull CssPseudoSelectorStub<PostCssCustomSelector> stub) {
    return new PostCssCustomSelectorImpl(stub, this);
  }
}