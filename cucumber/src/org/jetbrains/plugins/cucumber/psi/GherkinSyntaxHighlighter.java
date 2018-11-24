package org.jetbrains.plugins.cucumber.psi;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static org.jetbrains.plugins.cucumber.psi.GherkinTokenTypes.*;

/**
 * @author yole
 */
public class GherkinSyntaxHighlighter extends SyntaxHighlighterBase {
  private static final Map<IElementType, TextAttributesKey> ATTRIBUTES = new HashMap<>();

  private final GherkinKeywordProvider myKeywordProvider;

  public GherkinSyntaxHighlighter(GherkinKeywordProvider keywordProvider) {
    myKeywordProvider = keywordProvider;
  }

  static {
    ATTRIBUTES.put(COMMENT, GherkinHighlighter.COMMENT);
    ATTRIBUTES.put(TEXT, GherkinHighlighter.TEXT);
    ATTRIBUTES.put(STEP_KEYWORD, GherkinHighlighter.KEYWORD);
    ATTRIBUTES.put(TAG, GherkinHighlighter.TAG);
    ATTRIBUTES.put(FEATURE_KEYWORD, GherkinHighlighter.KEYWORD);
    ATTRIBUTES.put(SCENARIO_KEYWORD, GherkinHighlighter.KEYWORD);
    ATTRIBUTES.put(BACKGROUND_KEYWORD, GherkinHighlighter.KEYWORD);
    ATTRIBUTES.put(EXAMPLES_KEYWORD, GherkinHighlighter.KEYWORD);
    ATTRIBUTES.put(SCENARIO_OUTLINE_KEYWORD, GherkinHighlighter.KEYWORD);
    ATTRIBUTES.put(PYSTRING, GherkinHighlighter.PYSTRING);
    ATTRIBUTES.put(PYSTRING_TEXT, GherkinHighlighter.PYSTRING);
    ATTRIBUTES.put(TABLE_CELL, GherkinHighlighter.TABLE_CELL);
    ATTRIBUTES.put(PIPE, GherkinHighlighter.PIPE);
    ATTRIBUTES.put(COLON, GherkinHighlighter.KEYWORD);
  }

  @NotNull
  public Lexer getHighlightingLexer() {
    return new GherkinLexer(myKeywordProvider);
  }

  @NotNull
  public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
    return SyntaxHighlighterBase.pack(ATTRIBUTES.get(tokenType));
  }
}
