// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.highlight;

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;


public final class DroolsSyntaxHighlighterColors {
  public static final String DROOLS_RULE = "DROOLS_RULE";
  public static final String DROOLS_FUNCTION = "DROOLS_FUNCTION";
  public static final String DROOLS_LOCAL_VARIABLE = "DROOLS_LOCAL_VARIABLE";
  public static final String DROOLS_PUBLIC_STATIC_FIELD = "DROOLS_PUBLIC_STATIC";

  public static final TextAttributesKey LINE_COMMENT = createTextAttributesKey("DROOLS_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
  public static final TextAttributesKey BLOCK_COMMENT = createTextAttributesKey("DROOLS_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
  public static final TextAttributesKey DOC_COMMENT = createTextAttributesKey("DROOLS_DOC_COMMENT", DefaultLanguageHighlighterColors.DOC_COMMENT);

  public static final TextAttributesKey KEYWORD = createTextAttributesKey("DROOLS_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
  public static final TextAttributesKey ATTRIBUTES = createTextAttributesKey("DROOLS_ATTRIBUTES", DefaultLanguageHighlighterColors.KEYWORD);

  public static final TextAttributesKey OPERATIONS = createTextAttributesKey("DROOLS_OPERATIONS", DefaultLanguageHighlighterColors.OPERATION_SIGN);

  public static final TextAttributesKey KEYWORD_OPERATIONS = createTextAttributesKey("DROOLS_KEYWORD_OPERATIONS", DefaultLanguageHighlighterColors.KEYWORD);
  public static final TextAttributesKey NUMBER = createTextAttributesKey("DROOLS_NUMBER", DefaultLanguageHighlighterColors.NUMBER);
  public static final TextAttributesKey STRING = createTextAttributesKey("DROOLS_STRING", DefaultLanguageHighlighterColors.STRING);
  public static final TextAttributesKey OPERATION_SIGN = createTextAttributesKey("DROOLS_OPERATION_SIGN", DefaultLanguageHighlighterColors.OPERATION_SIGN);
  public static final TextAttributesKey PARENTHS = createTextAttributesKey("DROOLS_PARENTH", DefaultLanguageHighlighterColors.PARENTHESES);
  public static final TextAttributesKey BRACKETS = createTextAttributesKey("DROOLS_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS);
  public static final TextAttributesKey BRACES = createTextAttributesKey("DROOLS_BRACES", DefaultLanguageHighlighterColors.BRACES);
  public static final TextAttributesKey COMMA = createTextAttributesKey("DROOLS_COMMA", DefaultLanguageHighlighterColors.COMMA);
  public static final TextAttributesKey DOT = createTextAttributesKey("DROOLS_DOT", DefaultLanguageHighlighterColors.DOT);
  public static final TextAttributesKey SEMICOLON = createTextAttributesKey("DROOLS_SEMICOLON", DefaultLanguageHighlighterColors.SEMICOLON);
  public static final TextAttributesKey BAD_CHARACTER = createTextAttributesKey("DROOLS_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER);
  public static final TextAttributesKey RULE = createTextAttributesKey(DROOLS_RULE, DefaultLanguageHighlighterColors.CLASS_NAME);
  public static final TextAttributesKey LOCAL_VARIABLE =  createTextAttributesKey(DROOLS_LOCAL_VARIABLE, DefaultLanguageHighlighterColors.LOCAL_VARIABLE);
  public static final TextAttributesKey FUNCTION = createTextAttributesKey(DROOLS_FUNCTION, DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
  public static final TextAttributesKey FIELD = createTextAttributesKey("FIELD", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
  public static final TextAttributesKey PUBLIC_STATIC_FIELD =  createTextAttributesKey(DROOLS_PUBLIC_STATIC_FIELD, DefaultLanguageHighlighterColors.CONSTANT);
}
