package org.intellij.plugins.postcss.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.css.impl.CssPseudoSelectorBase;
import com.intellij.psi.css.impl.stubs.CssPseudoSelectorStub;
import com.intellij.psi.css.impl.stubs.base.CssNamedStubElementType;
import org.intellij.plugins.postcss.psi.PostCssCustomSelector;
import org.jetbrains.annotations.NotNull;

public class PostCssCustomSelectorImpl extends CssPseudoSelectorBase implements PostCssCustomSelector {

  public PostCssCustomSelectorImpl(@NotNull CssPseudoSelectorStub<PostCssCustomSelector> stub, @NotNull CssNamedStubElementType nodeType) {
    super(stub, nodeType);
  }

  public PostCssCustomSelectorImpl(@NotNull ASTNode node) {
    super(node);
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