// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.parser;

import com.intellij.ide.highlighter.custom.AbstractCustomLexer;
import com.intellij.ide.highlighter.custom.tokens.TokenParser;
import com.intellij.lang.PsiBuilder;
import com.intellij.lexer.Lexer;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.lang.Angular2EmbeddedContentTokenType;
import org.angular2.lang.expr.Angular2Language;
import org.jetbrains.annotations.NotNull;

import static com.intellij.lang.javascript.JSStubElementTypes.VAR_STATEMENT;
import static com.intellij.lang.javascript.JSTokenTypes.IDENTIFIER;
import static com.intellij.psi.xml.XmlTokenType.XML_NAME;
import static org.angular2.lang.html.parser.Angular2HtmlElementTypes.REFERENCE_VARIABLE;

public class Angular2HtmlReferenceTokenType extends Angular2EmbeddedContentTokenType {

  public static final Angular2HtmlReferenceTokenType INSTANCE = new Angular2HtmlReferenceTokenType();

  protected Angular2HtmlReferenceTokenType() {
    super("NG:REFERENCE_TOKEN", Angular2Language.INSTANCE);
  }

  @NotNull
  @Override
  protected Lexer createLexer() {
    return new AbstractCustomLexer(ContainerUtil.newArrayList(
      new RefPrefixTokenParser(), new VarIdentTokenParser()));
  }

  @Override
  protected void parse(@NotNull PsiBuilder builder) {
    assert builder.getTokenType() == XML_NAME;
    PsiBuilder.Marker start = builder.mark();
    builder.advanceLexer();
    PsiBuilder.Marker var = builder.mark();
    builder.advanceLexer();
    var.done(REFERENCE_VARIABLE);
    var.precede().done(VAR_STATEMENT);
    start.done(XML_NAME);
  }

  private static class VarIdentTokenParser extends TokenParser {
    @Override
    public boolean hasToken(int position) {
      if (position == myStartOffset) {
        return false;
      }
      myTokenInfo.updateData(position, myEndOffset, IDENTIFIER);
      return true;
    }
  }

  private static class RefPrefixTokenParser extends TokenParser {
    @Override
    public boolean hasToken(int position) {
      if (position == myStartOffset) {
        myTokenInfo.updateData(position, position + (myBuffer.charAt(0) == '#' ? 1 : 4), XML_NAME);
        return true;
      }
      return false;
    }
  }
}
