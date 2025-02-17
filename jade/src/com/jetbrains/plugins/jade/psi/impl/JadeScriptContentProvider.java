package com.jetbrains.plugins.jade.psi.impl;

import com.intellij.application.options.CodeStyle;
import com.intellij.lang.HtmlScriptContentProvider;
import com.intellij.lexer.Lexer;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.plugins.jade.highlighter.JadeSyntaxHighlighter;
import com.jetbrains.plugins.jade.psi.JadeTokenTypes;
import org.jetbrains.annotations.Nullable;

public final class JadeScriptContentProvider implements HtmlScriptContentProvider {
  @Override
  public IElementType getScriptElementType() {
    return JadeTokenTypes.FULL_JADE_EMBEDDED_CONTENT;
  }

  @Override
  public @Nullable Lexer getHighlightingLexer() {
    return new JadeSyntaxHighlighter(CodeStyle.getDefaultSettings()).getHighlightingLexer();
  }
}
