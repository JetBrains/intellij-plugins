package org.intellij.plugins.postcss.psi.impl;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.css.CssSelectorList;
import com.intellij.psi.css.descriptor.CssContextType;
import com.intellij.psi.css.impl.AtRulePresentation;
import com.intellij.psi.css.impl.CssAtRuleImpl;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.plugins.postcss.PostCssElementTypes;
import org.intellij.plugins.postcss.psi.PostCssCustomSelector;
import org.intellij.plugins.postcss.psi.PostCssCustomSelectorAtRule;
import org.intellij.plugins.postcss.psi.PostCssPsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PostCssCustomSelectorAtRuleImpl extends CssAtRuleImpl implements PostCssCustomSelectorAtRule {
  PostCssCustomSelectorAtRuleImpl() {
    super(CssContextType.ANY, PostCssElementTypes.POST_CSS_CUSTOM_SELECTOR_RULE);
  }

  @Override
  public @NotNull ItemPresentation getPresentation() {
    PostCssCustomSelector customSelector = getCustomSelector();
    return new AtRulePresentation(this, PostCssPsiUtil.isEmptyElement(customSelector)
                                        ? "custom-selector" : "custom-selector " + customSelector.getText());
  }

  @Override
  public @Nullable PostCssCustomSelector getCustomSelector() {
    return PsiTreeUtil.getChildOfType(this, PostCssCustomSelector.class);
  }

  @Override
  public @Nullable CssSelectorList getSelectorList() {
    return PsiTreeUtil.getChildOfType(this, CssSelectorList.class);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PostCssElementVisitor) {
      ((PostCssElementVisitor)visitor).visitPostCssCustomSelectorAtRule(this);
    }
    else {
      visitor.visitElement(this);
    }
  }
}