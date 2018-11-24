package com.dmarcotte.handlebars;

import com.dmarcotte.handlebars.parsing.HbRawLexer;
import com.dmarcotte.handlebars.parsing.HbTokenTypes;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class HbHighlighter extends SyntaxHighlighterBase {
  private static final Map<IElementType, TextAttributesKey> keys1;
  private static final Map<IElementType, TextAttributesKey> keys2;

  @NotNull
  public Lexer getHighlightingLexer() {
    return new HbRawLexer();
  }

  private static final TextAttributesKey MUSTACHES = TextAttributesKey.createTextAttributesKey(
    "HANDLEBARS.MUSTACHES",
    DefaultLanguageHighlighterColors.BRACES
  );

  private static final TextAttributesKey IDENTIFIERS = TextAttributesKey.createTextAttributesKey(
    "HANDLEBARS.IDENTIFIERS",
    DefaultLanguageHighlighterColors.KEYWORD
  );

  private static final TextAttributesKey COMMENTS = TextAttributesKey.createTextAttributesKey(
    "HANDLEBARS.COMMENTS",
    DefaultLanguageHighlighterColors.BLOCK_COMMENT
  );

  private static final TextAttributesKey OPERATORS = TextAttributesKey.createTextAttributesKey(
    "HANDLEBARS.OPERATORS",
    DefaultLanguageHighlighterColors.OPERATION_SIGN
  );

  private static final TextAttributesKey VALUES = TextAttributesKey.createTextAttributesKey(
    "HANDLEBARS.VALUES",
    DefaultLanguageHighlighterColors.NUMBER
  );

  private static final TextAttributesKey STRINGS = TextAttributesKey.createTextAttributesKey(
    "HANDLEBARS.STRINGS",
    DefaultLanguageHighlighterColors.STRING
  );

  private static final TextAttributesKey DATA_PREFIX = TextAttributesKey.createTextAttributesKey(
    "HANDLEBARS.DATA_PREFIX",
    DefaultLanguageHighlighterColors.KEYWORD
  );

  private static final TextAttributesKey ESCAPE = TextAttributesKey.createTextAttributesKey(
    "HANDLEBARS.ESCAPE",
    DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE
  );

  static {
    keys1 = new HashMap<>();
    keys2 = new HashMap<>();

    keys1.put(HbTokenTypes.OPEN, MUSTACHES);
    keys1.put(HbTokenTypes.OPEN_BLOCK, MUSTACHES);
    keys1.put(HbTokenTypes.OPEN_PARTIAL, MUSTACHES);
    keys1.put(HbTokenTypes.OPEN_ENDBLOCK, MUSTACHES);
    keys1.put(HbTokenTypes.OPEN_INVERSE, MUSTACHES);
    keys1.put(HbTokenTypes.OPEN_UNESCAPED, MUSTACHES);
    keys1.put(HbTokenTypes.CLOSE_UNESCAPED, MUSTACHES);
    keys1.put(HbTokenTypes.CLOSE, MUSTACHES);
    keys1.put(HbTokenTypes.ID, IDENTIFIERS);
    keys1.put(HbTokenTypes.COMMENT, COMMENTS);
    keys1.put(HbTokenTypes.COMMENT_OPEN, COMMENTS);
    keys1.put(HbTokenTypes.COMMENT_CLOSE, COMMENTS);
    keys1.put(HbTokenTypes.COMMENT_CONTENT, COMMENTS);
    keys1.put(HbTokenTypes.UNCLOSED_COMMENT, COMMENTS);
    keys1.put(HbTokenTypes.EQUALS, OPERATORS);
    keys1.put(HbTokenTypes.SEP, OPERATORS);
    keys1.put(HbTokenTypes.NUMBER, VALUES);
    keys1.put(HbTokenTypes.ELSE, IDENTIFIERS);
    keys1.put(HbTokenTypes.BOOLEAN, VALUES);
    keys1.put(HbTokenTypes.STRING, STRINGS);
    keys1.put(HbTokenTypes.DATA_PREFIX, DATA_PREFIX);
    keys1.put(HbTokenTypes.ESCAPE_CHAR, ESCAPE);
  }

  @NotNull
  public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
    return pack(keys1.get(tokenType), keys2.get(tokenType));
  }

  public static final Map<TextAttributesKey, Pair<String, HighlightSeverity>> DISPLAY_NAMES
    = new LinkedHashMap<>();

  static {
    DISPLAY_NAMES.put(MUSTACHES, new Pair<>(HbBundle.message("hb.page.colors.descriptor.mustaches.key"), null));
    DISPLAY_NAMES
      .put(IDENTIFIERS, new Pair<>(HbBundle.message("hb.page.colors.descriptor.identifiers.key"), null));
    DISPLAY_NAMES.put(COMMENTS, new Pair<>(HbBundle.message("hb.page.colors.descriptor.comments.key"), null));
    DISPLAY_NAMES.put(OPERATORS, new Pair<>(HbBundle.message("hb.page.colors.descriptor.operators.key"), null));
    DISPLAY_NAMES.put(VALUES, new Pair<>(HbBundle.message("hb.page.colors.descriptor.values.key"), null));
    DISPLAY_NAMES.put(STRINGS, new Pair<>(HbBundle.message("hb.page.colors.descriptor.strings.key"), null));
    DISPLAY_NAMES
      .put(DATA_PREFIX, new Pair<>(HbBundle.message("hb.page.colors.descriptor.data.prefix.key"), null));
    DISPLAY_NAMES.put(ESCAPE, new Pair<>(HbBundle.message("hb.page.colors.descriptor.escape.key"), null));
  }
}
