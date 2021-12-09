package com.intellij.tapestry.lang;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.tapestry.psi.TelLexer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.intellij.tapestry.intellij.lang.TemplateColorSettingsPage.*;
import static com.intellij.tapestry.psi.TelTokenTypes.*;

/**
 * @author Alexey Chmutov
 */
public final class TelHighlighter extends SyntaxHighlighterBase {
  @Override
  @NotNull
  public Lexer getHighlightingLexer() {
    return new TelLexer();
  }

  @Override
  public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
    return getTokenHighlightsStatic(tokenType);
  }

  public static TextAttributesKey[] getTokenHighlightsStatic(IElementType tokenType) {
    return SyntaxHighlighterBase.pack(ourMap.get(tokenType), TEL_BACKGROUND);
  }

  private static final Map<IElementType, TextAttributesKey> ourMap;

  static {
    ourMap = new HashMap<>();
    SyntaxHighlighterBase.fillMap(ourMap, TEL_BOUNDS, TAP5_EL_START, TAP5_EL_END);
    SyntaxHighlighterBase.fillMap(ourMap, TEL_IDENT, TAP5_EL_IDENTIFIER);
    SyntaxHighlighterBase.fillMap(ourMap, TEL_DOT, TAP5_EL_DOT, TAP5_EL_COLON, TAP5_EL_COMMA, TAP5_EL_QUESTION_DOT, TAP5_EL_RANGE, TAP5_EL_EXCLAMATION);
    SyntaxHighlighterBase.fillMap(ourMap, TEL_NUMBER, TAP5_EL_INTEGER, TAP5_EL_DECIMAL);
    SyntaxHighlighterBase.fillMap(ourMap, TEL_PARENTHS, TAP5_EL_LEFT_PARENTH, TAP5_EL_RIGHT_PARENTH);
    SyntaxHighlighterBase.fillMap(ourMap, TEL_BRACKETS, TAP5_EL_LEFT_BRACKET, TAP5_EL_RIGHT_BRACKET);
    SyntaxHighlighterBase.fillMap(ourMap, TEL_STRING, TAP5_EL_STRING);
    SyntaxHighlighterBase.fillMap(ourMap, TEL_BAD_CHAR, TAP5_EL_BAD_CHAR);
  }

}
