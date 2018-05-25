package org.intellij.plugins.postcss;

import com.intellij.lexer.EmbeddedTokenTypesProvider;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

public class PostCssEmbeddedTokenTypesProvider implements EmbeddedTokenTypesProvider {
  @NotNull
  @Override
  public String getName() {
    return "postcss";
  }

  @NotNull
  @Override
  public IElementType getElementType() {
    return PostCssElementTypes.POST_CSS_LAZY_STYLESHEET;
  }
}