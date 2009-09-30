package com.intellij.tapestry.lang;

import com.intellij.ide.highlighter.XmlFileHighlighter;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.tree.IElementType;
import com.intellij.tapestry.psi.TelTokenType;
import com.intellij.tapestry.psi.TmlHighlightingLexer;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 *         Date: Jun 23, 2009
 *         Time: 2:48:18 PM
 */
public class TmlHighlighter extends XmlFileHighlighter {

  @NotNull
  @Override
  public Lexer getHighlightingLexer() {
    return new TmlHighlightingLexer();
  }

  @Override
  @NotNull
  public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
    return tokenType instanceof TelTokenType
           ? TelHighlighter.getTokenHighlightsStatic(tokenType)
           : super.getTokenHighlights(tokenType);
  }

}

