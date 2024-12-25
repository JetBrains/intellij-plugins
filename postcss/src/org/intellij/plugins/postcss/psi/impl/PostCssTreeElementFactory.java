package org.intellij.plugins.postcss.psi.impl;

import com.intellij.psi.css.impl.CssTreeElementFactory;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.tree.IElementType;
import org.intellij.plugins.postcss.PostCssElementTypes;
import org.intellij.plugins.postcss.lexer.PostCssTokenTypes;
import org.jetbrains.annotations.NotNull;

public class PostCssTreeElementFactory extends CssTreeElementFactory {

  @Override
  protected boolean isComment(IElementType type) {
    return PostCssTokenTypes.POST_CSS_COMMENTS.contains(type);
  }

  @Override
  public @NotNull CompositeElement createComposite(final @NotNull IElementType type) {
    if (type == PostCssElementTypes.POST_CSS_NEST) {
      return new PostCssNestImpl();
    }
    else if (type == PostCssElementTypes.POST_CSS_CUSTOM_SELECTOR_RULE) {
      return new PostCssCustomSelectorAtRuleImpl();
    }
    else if (type == PostCssElementTypes.POST_CSS_CUSTOM_MEDIA_RULE) {
      return new PostCssCustomMediaAtRuleImpl();
    }
    else if (type == PostCssElementTypes.POST_CSS_SIMPLE_VARIABLE) {
      return new PostCssSimpleVariableImpl();
    }
    else if (type == PostCssElementTypes.POST_CSS_SIMPLE_VARIABLE_DECLARATION) {
      return new PostCssSimpleVariableDeclarationImpl();
    }
    return super.createComposite(type);
  }
}
