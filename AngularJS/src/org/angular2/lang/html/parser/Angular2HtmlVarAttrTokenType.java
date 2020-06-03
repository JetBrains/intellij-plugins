// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.parser;

import com.intellij.ide.highlighter.custom.AbstractCustomLexer;
import com.intellij.ide.highlighter.custom.tokens.TokenParser;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.javascript.psi.JSStubElementType;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.stubs.JSVariableStub;
import com.intellij.lexer.Lexer;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.lang.Angular2EmbeddedContentTokenType;
import org.angular2.lang.expr.Angular2Language;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static com.intellij.lang.javascript.JSStubElementTypes.VAR_STATEMENT;
import static com.intellij.lang.javascript.JSTokenTypes.IDENTIFIER;
import static com.intellij.psi.xml.XmlTokenType.XML_NAME;
import static org.angular2.lang.html.stub.Angular2HtmlStubElementTypes.LET_VARIABLE;
import static org.angular2.lang.html.stub.Angular2HtmlStubElementTypes.REFERENCE_VARIABLE;

public class Angular2HtmlVarAttrTokenType extends Angular2EmbeddedContentTokenType {

  public static final Angular2HtmlVarAttrTokenType REFERENCE = new Angular2HtmlVarAttrTokenType(
    "NG:REFERENCE_TOKEN", REFERENCE_VARIABLE, () -> new RefPrefixTokenParser());
  public static final Angular2HtmlVarAttrTokenType LET = new Angular2HtmlVarAttrTokenType(
    "NG:LET_TOKEN", LET_VARIABLE, () -> new LetPrefixTokenParser());


  private final JSStubElementType<JSVariableStub<JSVariable>, JSVariable> myVarElementType;
  private final Supplier<? extends TokenParser> myPrefixTokenParserConstructor;

  protected Angular2HtmlVarAttrTokenType(String debugName,
                                         JSStubElementType<JSVariableStub<JSVariable>, JSVariable> type,
                                         Supplier<? extends TokenParser> prefixTokenParserConstructor) {
    super(debugName, Angular2Language.INSTANCE);
    myVarElementType = type;
    myPrefixTokenParserConstructor = prefixTokenParserConstructor;
  }

  @Override
  protected @NotNull Lexer createLexer() {
    return new AbstractCustomLexer(ContainerUtil.newArrayList(
      myPrefixTokenParserConstructor.get(), new VarIdentTokenParser()));
  }

  @Override
  protected void parse(@NotNull PsiBuilder builder) {
    assert builder.getTokenType() == XML_NAME;
    PsiBuilder.Marker start = builder.mark();
    builder.advanceLexer();
    PsiBuilder.Marker var = builder.mark();
    builder.advanceLexer();
    var.done(myVarElementType);
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

  private static class LetPrefixTokenParser extends TokenParser {
    @Override
    public boolean hasToken(int position) {
      if (position == myStartOffset) {
        myTokenInfo.updateData(position, position + 4, XML_NAME);
        return true;
      }
      return false;
    }
  }
}
