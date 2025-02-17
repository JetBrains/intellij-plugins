// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.lexer;

import com.intellij.lexer.DelegateLexer;
import com.intellij.lexer.Lexer;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.plugins.jade.psi.JadeTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

class JadeEmbeddingLanguagesLexerDecorator extends DelegateLexer {

  private enum AttrLexState {
    NOTHING,
    SEEN_NAME,
    SEEN_NAME_AND_EQ
  }

  private @Nullable IElementType myFilterCodeReplacementElementType = null;
  private @Nullable IElementType myScriptBlockReplacementElementType = null;

  private boolean myIsLastTagNameWasScript;
  private @NotNull AttrLexState myAttrLexState = AttrLexState.NOTHING;

  JadeEmbeddingLanguagesLexerDecorator(@NotNull Lexer delegate) {
    super(delegate);
  }

  @Override
  public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
    super.start(buffer, startOffset, endOffset, initialState);

    processTokenAndChangeState(getTokenType());
  }

  @Override
  public void advance() {
    super.advance();

    processTokenAndChangeState(getTokenType());
  }

  private void processTokenAndChangeState(IElementType tokenType) {
    if (tokenType == JadeTokenTypes.FILTER_NAME) {
      myFilterCodeReplacementElementType = JadeEmbeddingUtil.getElementToEmbedForFilterName(getTokenText());
    }
    else if (tokenType == JadeTokenTypes.TAG_NAME) {
      myIsLastTagNameWasScript = "script".equals(getTokenText());
    }
    else if (myIsLastTagNameWasScript) {

      if (tokenType == JadeTokenTypes.ATTRIBUTE_NAME) {
        myAttrLexState = "type".equals(getTokenText()) ? AttrLexState.SEEN_NAME : AttrLexState.NOTHING;
      }
      else if (myAttrLexState == AttrLexState.SEEN_NAME &&
               (tokenType == JadeTokenTypes.EQ || tokenType == JadeTokenTypes.NEQ)) {
        myAttrLexState = AttrLexState.SEEN_NAME_AND_EQ;
      }
      else if (myAttrLexState == AttrLexState.SEEN_NAME_AND_EQ && tokenType == JadeTokenTypes.JS_EXPR) {
        myAttrLexState = AttrLexState.NOTHING;
        myScriptBlockReplacementElementType =
          JadeEmbeddingUtil.getElementToEmbedForATag("script", Collections.singletonMap("type", getTokenText()));
      }
      else {
        myAttrLexState = AttrLexState.NOTHING;
      }
    }
  }

  @Override
  public IElementType getTokenType() {
    final IElementType type = super.getTokenType();
    boolean isEmbeddedCode = false;
    IElementType result = type;

    if (type == JadeTokenTypes.FILTER_CODE && myFilterCodeReplacementElementType != null) {
      result = myFilterCodeReplacementElementType;
      isEmbeddedCode = true;
    }
    else if (type == JadeTokenTypes.JS_CODE_BLOCK && myScriptBlockReplacementElementType != null) {
      result = myScriptBlockReplacementElementType;
      isEmbeddedCode = true;
    }

    if (isEmbeddedCode && result != JadeTokenTypes.JS_CODE_BLOCK
        || result == JadeTokenTypes.STYLE_BLOCK) {
      result = JadeEmbeddingUtil.getEmbeddedTokenWrapperType(result);
    }

    return result;
  }
}
