// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.lexer;

import com.intellij.embedding.EmbeddingUtil;
import com.intellij.embedding.TemplateMasqueradingLexer;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lexer.Lexer;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.plugins.jade.js.JavaScriptInJadeLexer;
import com.jetbrains.plugins.jade.psi.JadeTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JSMetaCodeLexer extends TemplateMasqueradingLexer {
  public JSMetaCodeLexer(@NotNull ASTNode chameleon) {
    super(new MyJadeJSLexer(EmbeddingUtil.calcBaseIndent(chameleon), new JavaScriptInJadeLexer()));
  }

  public JSMetaCodeLexer() {
    super(new MyJadeJSLexer(0, new JavaScriptInJadeLexer()));
  }

  @Override
  public @Nullable IElementType getMasqueTokenType() {
    final IElementType type = getTokenType();
    if (type == MINUS_TYPE) {
      return null;
    }
    else if (type == JadeTokenTypes.INDENT) {
      return TokenType.WHITE_SPACE;
    }
    if (type == JadeTokenTypes.JADE_EMBEDDED_CONTENT) {
      return JSTokenTypes.SEMICOLON;
    }
    return type;
  }

  @Override
  public @Nullable String getMasqueTokenText() {
    final IElementType type = getTokenType();
    if (type == MINUS_TYPE) {
      return null;
    }
    else if (type == JadeTokenTypes.INDENT) {
      return "\n";
    }
    if (type == JadeTokenTypes.JADE_EMBEDDED_CONTENT) {
      return ";";
    }

    return myDelegate.getTokenText();
  }

  private static class MyJadeJSLexer extends TemplateMasqueradingLexer.MyLexer {
    MyJadeJSLexer(int indent, Lexer delegateLexer) {
      super(indent, delegateLexer);
    }

    @Override
    protected IElementType getIndentTokenType() {
      return JadeTokenTypes.INDENT;
    }

    @Override
    protected IElementType getEmbeddedContentTokenType() {
      return JadeTokenTypes.JADE_EMBEDDED_CONTENT;
    }

    @Override
    protected int getEmbeddedCodeStartMarkerLength() {
      char c = myBuffer.charAt(myTokenStart);
      return c == '-' ? 1 : 0;
    }
  }
}
