package org.intellij.plugins.postcss.psi.impl;

import com.intellij.psi.css.impl.CssTreeElementFactory;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.tree.IElementType;
import org.intellij.plugins.postcss.PostCssElementTypes;
import org.jetbrains.annotations.NotNull;

public class PostCssTreeElementFactory extends CssTreeElementFactory {

  @NotNull
  public CompositeElement createComposite(final IElementType type) {
    if (type == PostCssElementTypes.POST_CSS_NEST) {
      return new PostCssNestImpl();
    }
    else if (type == PostCssElementTypes.POST_CSS_CUSTOM_SELECTOR_RULE) {
      return new PostCssCustomSelectorAtRuleImpl();
    }
    else if (type == PostCssElementTypes.POST_CSS_CUSTOM_MEDIA_RULE) {
      return new PostCssCustomMediaAtRuleImpl();
    }
    else if (type == PostCssElementTypes.POST_CSS_APPLY_RULE) {
      return new PostCssApplyAtRuleImpl();
    }
    return super.createComposite(type);
  }
}
