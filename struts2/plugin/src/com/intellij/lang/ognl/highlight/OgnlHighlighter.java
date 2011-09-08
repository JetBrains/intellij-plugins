/*
 * Copyright 2011 The authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.lang.ognl.highlight;

import com.intellij.ide.highlighter.HtmlFileHighlighter;
import com.intellij.ide.highlighter.XmlFileHighlighter;
import com.intellij.lang.ognl.psi.OgnlTokenTypes;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.JspHighlighterColors;
import com.intellij.openapi.editor.SyntaxHighlighterColors;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.StringEscapesTokenTypes;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides basic syntax highlighting.
 *
 * @author Yann C&eacute;bron
 */
public class OgnlHighlighter extends SyntaxHighlighterBase {

  private static final Map<IElementType, TextAttributesKey> keys1;

  @NotNull
  @Override
  public Lexer getHighlightingLexer() {
    return new OgnlHighlightingLexer();
  }

  @NotNull
  @Override
  public TextAttributesKey[] getTokenHighlights(final IElementType iElementType) {
    return pack(BACKGROUND, keys1.get(iElementType));
  }

  public static final TextAttributesKey BACKGROUND = TextAttributesKey.createTextAttributesKey(
      "OGNL.BACKGROUND",
      JspHighlighterColors.JSP_ACTION_AND_DIRECTIVE_BACKGROUND.getDefaultAttributes());

  public static final TextAttributesKey EXPRESSION = TextAttributesKey.createTextAttributesKey(
      "OGNL.EXPRESSION",
      SyntaxHighlighterColors.KEYWORD.getDefaultAttributes());

  public static final TextAttributesKey BAD_CHARACTER = TextAttributesKey.createTextAttributesKey(
      "OGNL.BAD_CHARACTER",
      HighlighterColors.BAD_CHARACTER.getDefaultAttributes());

  public static final TextAttributesKey IDENTIFIER = TextAttributesKey.createTextAttributesKey(
      "OGNL.IDENTIFIER",
      CodeInsightColors.LOCAL_VARIABLE_ATTRIBUTES.getDefaultAttributes());

  public static final TextAttributesKey KEYWORDS = TextAttributesKey.createTextAttributesKey(
      "OGNL.KEYWORDS",
      SyntaxHighlighterColors.KEYWORD.getDefaultAttributes());

  public static final TextAttributesKey OPERATIONS = TextAttributesKey.createTextAttributesKey(
      "OGNL.OPERATIONS",
      SyntaxHighlighterColors.OPERATION_SIGN.getDefaultAttributes());

  public static final TextAttributesKey NUMBER = TextAttributesKey.createTextAttributesKey(
      "OGNL.NUMBER",
      SyntaxHighlighterColors.NUMBER.getDefaultAttributes());

  public static final TextAttributesKey STRING = TextAttributesKey.createTextAttributesKey(
      "OGNL.STRING",
      SyntaxHighlighterColors.STRING.getDefaultAttributes());

  public static final TextAttributesKey BRACKETS = TextAttributesKey.createTextAttributesKey(
      "OGNL.BRACKETS",
      SyntaxHighlighterColors.BRACKETS.getDefaultAttributes());

  public static final TextAttributesKey PARENTHS = TextAttributesKey.createTextAttributesKey(
      "OGNL.PARENTHS",
      SyntaxHighlighterColors.PARENTHS.getDefaultAttributes());

  public static final TextAttributesKey BRACES = TextAttributesKey.createTextAttributesKey(
      "OGNL.BRACES",
      SyntaxHighlighterColors.BRACES.getDefaultAttributes());

  static {
    keys1 = new HashMap<IElementType, TextAttributesKey>();

    // single characters
    keys1.put(OgnlTokenTypes.BAD_CHARACTER, BAD_CHARACTER);

    // EXPR_HOLDER
    keys1.put(OgnlTokenTypes.EXPRESSION_START, EXPRESSION);
    keys1.put(OgnlTokenTypes.EXPRESSION_END, EXPRESSION);

    keys1.put(OgnlTokenTypes.IDENTIFIER, IDENTIFIER);

    SyntaxHighlighterBase.fillMap(keys1, OgnlTokenTypes.KEYWORDS, KEYWORDS);
    SyntaxHighlighterBase.fillMap(keys1, OgnlTokenTypes.OPERATION_KEYWORDS, KEYWORDS);

    SyntaxHighlighterBase.fillMap(keys1, OgnlTokenTypes.OPERATION_SIGNS, OPERATIONS);

    // literals
    SyntaxHighlighterBase.fillMap(keys1, OgnlTokenTypes.NUMBERS, NUMBER);
    SyntaxHighlighterBase.fillMap(keys1, OgnlTokenTypes.TEXT, STRING);

    // string/character escape sequences
    keys1.put(StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN, SyntaxHighlighterColors.VALID_STRING_ESCAPE);
    keys1.put(StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN, SyntaxHighlighterColors.INVALID_STRING_ESCAPE);
    keys1.put(StringEscapesTokenTypes.INVALID_UNICODE_ESCAPE_TOKEN, SyntaxHighlighterColors.INVALID_STRING_ESCAPE);

    // braces
    keys1.put(OgnlTokenTypes.LBRACKET, BRACKETS);
    keys1.put(OgnlTokenTypes.RBRACKET, BRACKETS);

    keys1.put(OgnlTokenTypes.LPARENTH, PARENTHS);
    keys1.put(OgnlTokenTypes.RPARENTH, PARENTHS);

    keys1.put(OgnlTokenTypes.LBRACE, BRACES);
    keys1.put(OgnlTokenTypes.RBRACE, BRACES);

    XmlFileHighlighter.registerEmbeddedTokenAttributes(keys1, null);
    HtmlFileHighlighter.registerEmbeddedTokenAttributes(keys1, null);
  }

}