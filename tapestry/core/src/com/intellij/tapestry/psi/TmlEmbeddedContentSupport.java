package com.intellij.tapestry.psi;

import com.intellij.lexer.BaseHtmlLexer;
import com.intellij.lexer.HtmlEmbeddedContentProvider;
import com.intellij.lexer.HtmlEmbeddedContentSupport;
import com.intellij.lexer.HtmlTokenEmbeddedContentProvider;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class TmlEmbeddedContentSupport implements HtmlEmbeddedContentSupport {

  private static final TokenSet ATTRIBUTE_TOKENS = TokenSet.create(TelTokenTypes.TAP5_EL_CONTENT);

  @Override
  public boolean isEnabled(@NotNull BaseHtmlLexer lexer) {
    return lexer instanceof TmlLexer || lexer instanceof TmlHighlightingLexer;
  }

  @NotNull
  @Override
  public List<HtmlEmbeddedContentProvider> createEmbeddedContentProviders(@NotNull BaseHtmlLexer lexer) {
    return Collections.singletonList(new HtmlTokenEmbeddedContentProvider(
      lexer, TelTokenTypes.TAP5_EL_CONTENT, () -> new TelLexer(), () -> TelTokenTypes.TAP5_EL_HOLDER));
  }

  @NotNull
  @Override
  public TokenSet getCustomAttributeEmbedmentTokens() {
    return ATTRIBUTE_TOKENS;
  }
}
