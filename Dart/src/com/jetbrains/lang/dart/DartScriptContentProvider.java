// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart;

import com.intellij.lang.HtmlScriptContentProvider;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.Nullable;

public final class DartScriptContentProvider implements HtmlScriptContentProvider {
  @Override
  public IElementType getScriptElementType() {
    return DartTokenTypesSets.EMBEDDED_CONTENT;
  }

  @Override
  public @Nullable Lexer getHighlightingLexer() {
    SyntaxHighlighter syntaxHighlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(DartFileType.INSTANCE, null, null);
    return syntaxHighlighter != null ? syntaxHighlighter.getHighlightingLexer() : null;
  }
}
