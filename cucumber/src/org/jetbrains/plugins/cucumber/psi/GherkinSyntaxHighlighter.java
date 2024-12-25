// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.psi;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.jetbrains.plugins.cucumber.psi.GherkinTokenTypes.*;


public class GherkinSyntaxHighlighter extends SyntaxHighlighterBase {
  private static final Map<IElementType, TextAttributesKey> ATTRIBUTES = new HashMap<>();

  private final GherkinKeywordProvider myKeywordProvider;

  public GherkinSyntaxHighlighter(GherkinKeywordProvider keywordProvider) {
    myKeywordProvider = keywordProvider;
  }

  static {
    Arrays.stream(KEYWORDS.getTypes()).forEach(p -> ATTRIBUTES.put(p, GherkinHighlighter.KEYWORD));

    ATTRIBUTES.put(COMMENT, GherkinHighlighter.COMMENT);
    ATTRIBUTES.put(TEXT, GherkinHighlighter.TEXT);
    ATTRIBUTES.put(TAG, GherkinHighlighter.TAG);
    ATTRIBUTES.put(PYSTRING, GherkinHighlighter.PYSTRING);
    ATTRIBUTES.put(PYSTRING_TEXT, GherkinHighlighter.PYSTRING);
    ATTRIBUTES.put(TABLE_CELL, GherkinHighlighter.TABLE_CELL);
    ATTRIBUTES.put(PIPE, GherkinHighlighter.PIPE);
  }

  @Override
  public @NotNull Lexer getHighlightingLexer() {
    return new GherkinLexer(myKeywordProvider);
  }

  @Override
  public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
    return SyntaxHighlighterBase.pack(ATTRIBUTES.get(tokenType));
  }
}
