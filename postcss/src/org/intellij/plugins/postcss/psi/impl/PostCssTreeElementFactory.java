package org.intellij.plugins.postcss.psi.impl;

import com.intellij.psi.css.impl.CssTreeElementFactory;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.impl.source.tree.LazyParseableElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.ILazyParseableElementType;
import org.intellij.plugins.postcss.PostCssElementTypes;
import org.jetbrains.annotations.NotNull;

public class PostCssTreeElementFactory extends CssTreeElementFactory {

  @NotNull
  public CompositeElement createComposite(final IElementType type) {
    if (type == PostCssElementTypes.POST_CSS_DIRECT_NEST) {
      return new PostCssDirectNestImpl();
    }
    if (type == PostCssElementTypes.POST_CSS_NEST_SYM) {
      return new PostCssNestSymImpl();
    }
    return super.createComposite(type);
  }

  @Override
  public LazyParseableElement createLazy(final ILazyParseableElementType type, final CharSequence text) {
    return null;
  }
}
