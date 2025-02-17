package com.jetbrains.plugins.jade.highlighter;

import com.intellij.ide.highlighter.EmbeddedTokenHighlighter;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.MultiMap;
import com.jetbrains.plugins.jade.JadeLanguage;
import com.jetbrains.plugins.jade.lexer.JadeEmbeddingUtil;
import com.jetbrains.plugins.jade.lexer.JadeHighlightingLexer;
import com.jetbrains.plugins.jade.psi.JadeTokenTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class JadeSyntaxHighlighter extends SyntaxHighlighterBase implements SyntaxHighlighter {
  private static final Map<IElementType, TextAttributesKey> ATTRIBUTES = new ConcurrentHashMap<>();
  private final CodeStyleSettings myCodeStyleSettings;

  public JadeSyntaxHighlighter(CodeStyleSettings codeStyleSettings) {
    myCodeStyleSettings = codeStyleSettings;
  }

  static {
    put(JadeTokenTypes.COMMENT, JadeHighlighter.COMMENT);
    put(JadeTokenTypes.UNBUF_COMMENT, JadeHighlighter.UNBUF_COMMENT);
    put(JadeTokenTypes.DOCTYPE_KEYWORD, JadeHighlighter.DOCTYPE_KEYWORD);
    put(JadeTokenTypes.TEXT, JadeHighlighter.TEXT);
    put(JadeTokenTypes.STRING_LITERAL, JadeHighlighter.TEXT);
    put(JadeTokenTypes.CHAR_LITERAL, JadeHighlighter.TEXT);
    put(JadeTokenTypes.TAG_NAME, JadeHighlighter.TAG_NAME);
    put(JadeTokenTypes.ATTRIBUTE_NAME, JadeHighlighter.ATTRIBUTE_NAME);
    put(JadeTokenTypes.ATTRIBUTES_KEYWORD, JadeHighlighter.ATTRIBUTE_NAME);
    put(JadeTokenTypes.TAG_ID, JadeHighlighter.TAG_ID);
    put(JadeTokenTypes.TAG_CLASS, JadeHighlighter.TAG_CLASS);
    put(JadeTokenTypes.BAD_CHARACTER, JadeHighlighter.BAD_CHARACTER);
    put(JadeTokenTypes.NUMBER, JadeHighlighter.NUMBER);
    put(JadeTokenTypes.PIPE, JadeHighlighter.PIPE);
    put(JadeTokenTypes.COMMA, JadeHighlighter.COMMA);
    put(JadeTokenTypes.LPAREN, JadeHighlighter.PARENTS);
    put(JadeTokenTypes.RPAREN, JadeHighlighter.PARENTS);
    put(JadeTokenTypes.COLON, JadeHighlighter.COLON);
    put(JadeTokenTypes.MINUS, JadeHighlighter.OPERATION_SIGN);
    put(JadeTokenTypes.EQ, JadeHighlighter.OPERATION_SIGN);
    put(JadeTokenTypes.NEQ, JadeHighlighter.OPERATION_SIGN);
    put(JadeTokenTypes.COND_KEYWORD, JadeHighlighter.KEYWORD);
    put(JadeTokenTypes.ELSE_KEYWORD, JadeHighlighter.KEYWORD);
    put(JadeTokenTypes.ITERATION_KEYWORD, JadeHighlighter.KEYWORD);
    put(JadeTokenTypes.CASE, JadeHighlighter.KEYWORD);
    put(JadeTokenTypes.WHEN, JadeHighlighter.KEYWORD);
    put(JadeTokenTypes.DEFAULT_KEYWORD, JadeHighlighter.KEYWORD);
    put(JadeTokenTypes.EXTENDS_KEYWORD, JadeHighlighter.KEYWORD);
    put(JadeTokenTypes.FILE_PATH, JadeHighlighter.FILE_PATH);
    put(JadeTokenTypes.INCLUDE_KEYWORD, JadeHighlighter.KEYWORD);
    put(JadeTokenTypes.YIELD_KEYWORD, JadeHighlighter.KEYWORD);
    put(JadeTokenTypes.MIXIN_KEYWORD, JadeHighlighter.KEYWORD);
    put(JadeTokenTypes.FILTER_NAME, JadeHighlighter.FILTER_NAME);

    for (IElementType type : JadeTokenTypes.JS_TOKENS.getTypes()) {
      put(type, JadeHighlighter.JS_BLOCK);
    }
  }

  private static void put(IElementType token, TextAttributesKey value) {
    if (ATTRIBUTES.put(token, value) != null) {
      Logger.getInstance(JadeSyntaxHighlighter.class).error("ALREADY MAPPED: " + token);
    }
  }

  public static void registerEmbeddedToken(IElementType wrapper) {
    put(wrapper, JadeHighlighter.EMBEDDED_CONTENT);
  }

  public static void unregisterEmbeddedToken(IElementType wrapper) {
    ATTRIBUTES.remove(wrapper);
  }

  @Override
  public @NotNull Lexer getHighlightingLexer() {
    return new JadeHighlightingLexer(myCodeStyleSettings);
  }

  @Override
  public TextAttributesKey @NotNull [] getTokenHighlights(final IElementType tokenType) {
    final TextAttributesKey ownResult = ATTRIBUTES.get(tokenType);

    if (tokenType.getLanguage() == JadeLanguage.INSTANCE || ownResult != null) {
      return pack(ownResult);
    }

    return JadeEmbeddingUtil.getHighlighterForLanguage(tokenType.getLanguage()).getTokenHighlights(tokenType);
  }

  public static final class XmlTokenHighlighter implements EmbeddedTokenHighlighter {
    @Override
    public @NotNull MultiMap<IElementType, TextAttributesKey> getEmbeddedTokenAttributes() {
      MultiMap<IElementType, TextAttributesKey> map = MultiMap.create();
      map.putAllValues(ATTRIBUTES);
      return map;
    }
  }
}
