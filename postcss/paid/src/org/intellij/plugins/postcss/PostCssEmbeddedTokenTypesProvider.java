package org.intellij.plugins.postcss;

import com.intellij.lexer.EmbeddedTokenTypesProvider;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

public class PostCssEmbeddedTokenTypesProvider implements EmbeddedTokenTypesProvider {
  @Override
  public @NotNull String getName() {
    return "postcss";
  }

  @Override
  public @NotNull IElementType getElementType() {
    return PostCssElementTypes.POST_CSS_LAZY_STYLESHEET;
  }
}