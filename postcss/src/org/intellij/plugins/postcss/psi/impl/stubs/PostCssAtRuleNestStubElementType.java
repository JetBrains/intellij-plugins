package org.intellij.plugins.postcss.psi.impl.stubs;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.impl.stubs.CssStub;
import com.intellij.psi.css.impl.stubs.base.CssPrimitiveStubElementType;
import org.intellij.plugins.postcss.psi.PostCssAtRuleNest;
import org.intellij.plugins.postcss.psi.impl.PostCssAtRuleNestImpl;
import org.jetbrains.annotations.NotNull;

public class PostCssAtRuleNestStubElementType extends CssPrimitiveStubElementType<PostCssAtRuleNest> {
  public PostCssAtRuleNestStubElementType() {
    super("POST_CSS_AT_RULE_NEST");
  }

  @Override
  public PsiElement createElement(ASTNode node) {
    return new PostCssAtRuleNestImpl(node);
  }

  @Override
  public PostCssAtRuleNest createPsi(@NotNull CssStub stub) {
    return new PostCssAtRuleNestImpl(stub, this);
  }
}
