// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.highlighter;

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.XmlHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.jetbrains.plugins.jade.JadeLanguage;

import static com.intellij.openapi.editor.colors.EditorColors.createInjectedLanguageFragmentKey;

public final class JadeHighlighter {

  private static TextAttributesKey create(String name, TextAttributesKey origin) {
    return TextAttributesKey.createTextAttributesKey("JADE_" + name, origin);
  }

  public static final TextAttributesKey COMMENT = create("COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
  public static final TextAttributesKey UNBUF_COMMENT = create("UNBUF_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
  public static final TextAttributesKey DOCTYPE_KEYWORD = create("DOCTYPE", DefaultLanguageHighlighterColors.KEYWORD);
  public static final TextAttributesKey KEYWORD = create("KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
  public static final TextAttributesKey TEXT = create("TEXT", DefaultLanguageHighlighterColors.STRING);
  public static final TextAttributesKey TAG_NAME = create("TAG_NAME", XmlHighlighterColors.HTML_TAG_NAME);
  public static final TextAttributesKey ATTRIBUTE_NAME = create("ATTRIBUTE_NAME", XmlHighlighterColors.HTML_ATTRIBUTE_NAME);
  public static final TextAttributesKey BAD_CHARACTER = create("BAD_CHARACTER", HighlighterColors.BAD_CHARACTER);
  public static final TextAttributesKey TAG_ID = create("TAG_ID", XmlHighlighterColors.HTML_ATTRIBUTE_NAME);
  public static final TextAttributesKey TAG_CLASS = create("TAG_CLASS", DefaultLanguageHighlighterColors.STATIC_FIELD);
  public static final TextAttributesKey NUMBER = create("NUMBER", DefaultLanguageHighlighterColors.NUMBER);
  public static final TextAttributesKey PIPE = create("PIPE", DefaultLanguageHighlighterColors.DOT);
  public static final TextAttributesKey COMMA = create("COMMA", DefaultLanguageHighlighterColors.COMMA);
  public static final TextAttributesKey PARENTS = create("PARENTS", DefaultLanguageHighlighterColors.PARENTHESES);
  public static final TextAttributesKey COLON = create("COLON", DefaultLanguageHighlighterColors.SEMICOLON);
  public static final TextAttributesKey OPERATION_SIGN = create("OPERATION_SIGN", DefaultLanguageHighlighterColors.OPERATION_SIGN);
  public static final TextAttributesKey STATEMENTS = create("STATEMENTS", DefaultLanguageHighlighterColors.KEYWORD);
  public static final TextAttributesKey FILE_PATH = create("FILE_PATH", DefaultLanguageHighlighterColors.STRING);
  public static final TextAttributesKey FILTER_NAME = create("FILTER_NAME", DefaultLanguageHighlighterColors.LABEL);
  public static final TextAttributesKey JS_BLOCK = create("JS_BLOCK", DefaultLanguageHighlighterColors.IDENTIFIER);
  public static final TextAttributesKey EMBEDDED_CONTENT = create("EMBEDDED_CONTENT", DefaultLanguageHighlighterColors.TEMPLATE_LANGUAGE_COLOR);
  public static final TextAttributesKey INJECTED_LANGUAGE_FRAGMENT = createInjectedLanguageFragmentKey(JadeLanguage.INSTANCE);
}
