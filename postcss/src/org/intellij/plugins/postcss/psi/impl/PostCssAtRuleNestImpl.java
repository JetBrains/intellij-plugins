package org.intellij.plugins.postcss.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.css.CssSelectorList;
import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.css.impl.stubs.CssStub;
import com.intellij.psi.css.impl.stubs.base.CssStubElement;
import com.intellij.psi.css.impl.stubs.base.CssStubElementType;
import org.intellij.plugins.postcss.psi.PostCssAtRuleNest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PostCssAtRuleNestImpl extends CssStubElement<CssStub> implements PostCssAtRuleNest {
  public PostCssAtRuleNestImpl(@NotNull CssStub stub, @NotNull CssStubElementType nodeType) {
    super(stub, nodeType);
  }

  public PostCssAtRuleNestImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Nullable
  @Override
  public CssSelectorList getSelectorList() {
    return getStubOrPsiChild(CssElementTypes.CSS_SELECTOR_LIST);
  }

}
