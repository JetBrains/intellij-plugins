package org.intellij.plugins.postcss.psi.impl;

import com.intellij.psi.css.descriptor.CssContextType;
import com.intellij.psi.css.impl.CssAtRuleImpl;
import org.intellij.plugins.postcss.PostCssElementTypes;
import org.intellij.plugins.postcss.psi.PostCssCustomSelectorAtRule;

final public class PostCssCustomSelectorAtRuleImpl extends CssAtRuleImpl implements PostCssCustomSelectorAtRule {
  PostCssCustomSelectorAtRuleImpl() {
    super(CssContextType.ANY, PostCssElementTypes.POST_CSS_CUSTOM_SELECTOR_RULE);
  }
}