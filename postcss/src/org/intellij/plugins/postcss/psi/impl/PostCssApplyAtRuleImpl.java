package org.intellij.plugins.postcss.psi.impl;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.css.descriptor.CssContextType;
import com.intellij.psi.css.impl.AtRulePresentation;
import com.intellij.psi.css.impl.CssAtRuleImpl;
import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.css.impl.CssTokenImpl;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.plugins.postcss.PostCssElementTypes;
import org.intellij.plugins.postcss.psi.PostCssApplyAtRule;
import org.jetbrains.annotations.NotNull;

public class PostCssApplyAtRuleImpl extends CssAtRuleImpl implements PostCssApplyAtRule {
  PostCssApplyAtRuleImpl() {
    super(CssContextType.ANY, PostCssElementTypes.POST_CSS_APPLY_RULE);
  }

  @NotNull
  @Override
  public ItemPresentation getPresentation() {
    CssTokenImpl identifier = getCustomPropertiesSetIdentifier();
    return new AtRulePresentation(this, identifier == null ? "apply" : "apply " + identifier.getText());
  }

  @Override
  public CssTokenImpl getCustomPropertiesSetIdentifier() {
    CssTokenImpl identifier = PsiTreeUtil.getNextSiblingOfType(getFirstChild(), CssTokenImpl.class);
    return identifier != null && identifier.getNode().getElementType() == CssElementTypes.CSS_IDENT ? identifier : null;
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PostCssElementVisitor) {
      ((PostCssElementVisitor)visitor).visitPostCssApplyAtRule(this);
    }
    else {
      visitor.visitElement(this);
    }
  }
}