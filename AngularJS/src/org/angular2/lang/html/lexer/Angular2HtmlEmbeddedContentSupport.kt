// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html.lexer;

import com.intellij.html.embedding.*;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lexer.BaseHtmlLexer;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.MergeFunction;
import com.intellij.lexer.MergingLexerAdapterBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.lang.expr.highlighting.Angular2SyntaxHighlighter;
import org.angular2.lang.html.highlighting.Angular2HtmlHighlightingLexer;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.angular2.lang.html.parser.Angular2AttributeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import static org.angular2.lang.expr.parser.Angular2EmbeddedExprTokenType.INTERPOLATION_EXPR;
import static org.angular2.lang.html.highlighting.Angular2HtmlHighlightingLexer.EXPRESSION_WHITE_SPACE;

public class Angular2HtmlEmbeddedContentSupport implements HtmlEmbeddedContentSupport {

  public static final class Holder {
    public static final EnumSet<Angular2AttributeType> NG_EL_ATTRIBUTES = EnumSet.of(
      Angular2AttributeType.EVENT, Angular2AttributeType.BANANA_BOX_BINDING,
      Angular2AttributeType.PROPERTY_BINDING, Angular2AttributeType.TEMPLATE_BINDINGS);
  }

  @Override
  public boolean isEnabled(@NotNull BaseHtmlLexer lexer) {
    return lexer instanceof Angular2HtmlLexer || lexer instanceof Angular2HtmlHighlightingLexer;
  }

  @NotNull
  @Override
  public List<HtmlEmbeddedContentProvider> createEmbeddedContentProviders(@NotNull BaseHtmlLexer lexer) {
    return ContainerUtil.newArrayList(new HtmlTokenEmbeddedContentProvider(
                                        lexer, INTERPOLATION_EXPR, () -> new Angular2EmbeddedHighlightingLexer()),
                                      new Angular2AttributeContentProvider(lexer));
  }

  public static class Angular2AttributeContentProvider extends HtmlAttributeEmbeddedContentProvider {

    Angular2AttributeContentProvider(@NotNull BaseHtmlLexer lexer) {
      super(lexer);
    }

    @Nullable
    @Override
    protected HtmlEmbedmentInfo createEmbedmentInfo() {
      CharSequence attributeName = getAttributeName();
      if (attributeName == null) return null;
      Angular2AttributeNameParser.AttributeInfo info = Angular2AttributeNameParser.parse(
        attributeName.toString(), Objects.requireNonNull(getTagName()).toString());
      if (info.type != Angular2AttributeType.REGULAR
          && Holder.NG_EL_ATTRIBUTES.contains(info.type)) {
        return new HtmlEmbedmentInfo() {
          @Override
          public Lexer createHighlightingLexer() { return new Angular2EmbeddedHighlightingLexer(); }

          @Override
          public IElementType getElementType() { return null; }
        };
      }
      return null;
    }

    @Override
    protected boolean isInterestedInTag(@NotNull CharSequence tagName) {
      return getLexer() instanceof Angular2HtmlHighlightingLexer;
    }

    @Override
    protected boolean isInterestedInAttribute(@NotNull CharSequence attributeName) {
      return true;
    }
  }

  public static class Angular2EmbeddedHighlightingLexer extends MergingLexerAdapterBase {

    Angular2EmbeddedHighlightingLexer() {
      super(new Angular2SyntaxHighlighter().getHighlightingLexer());
    }

    @Override
    public MergeFunction getMergeFunction() {
      return (type, lexer) -> type == JSTokenTypes.WHITE_SPACE ? EXPRESSION_WHITE_SPACE : type;
    }
  }
}
