package com.intellij.tapestry.psi;

import com.intellij.html.embedding.HtmlEmbeddedContentProvider;
import com.intellij.html.embedding.HtmlEmbeddedContentSupport;
import com.intellij.html.embedding.HtmlTokenEmbeddedContentProvider;
import com.intellij.lexer.BaseHtmlLexer;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class TmlEmbeddedContentSupport implements HtmlEmbeddedContentSupport {

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

}
