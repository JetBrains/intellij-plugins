package com.intellij.tapestry.lang;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import static com.intellij.tapestry.intellij.lang.TemplateColorSettingsPage.*;
import com.intellij.tapestry.psi.TelElementTypes;
import com.intellij.tapestry.psi.TelLexer;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author Alexey Chmutov
 *         Date: Jun 22, 2009
 *         Time: 9:00:33 PM
 */
public class TelHighlighter extends SyntaxHighlighterBase {
  @NotNull
  public Lexer getHighlightingLexer() {
    return new TelLexer();
  }

  @NotNull
  public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
    return getTokenHighlightsStatic(tokenType);
  }

  public static TextAttributesKey[] getTokenHighlightsStatic(IElementType tokenType) {
    return SyntaxHighlighterBase.pack(ourMap.get(tokenType), TEL_BACKGROUND);
  }
  private static final Map<IElementType, TextAttributesKey> ourMap;

  static {
    ourMap = new THashMap<IElementType, TextAttributesKey>();
    SyntaxHighlighterBase.fillMap(ourMap, TEL_BOUNDS, TelElementTypes.TAP5_EL_START, TelElementTypes.TAP5_EL_END);
    SyntaxHighlighterBase.fillMap(ourMap, TEL_IDENT, TelElementTypes.TAP5_EL_IDENTIFIER);
    SyntaxHighlighterBase.fillMap(ourMap, TEL_DOT, TelElementTypes.TAP5_EL_DOT, TelElementTypes.TAP5_EL_COLON, TelElementTypes.TAP5_EL_COMMA);
    SyntaxHighlighterBase.fillMap(ourMap, TEL_BAD_CHAR, TelElementTypes.TAP5_EL_BAD_CHAR);
  }

}
