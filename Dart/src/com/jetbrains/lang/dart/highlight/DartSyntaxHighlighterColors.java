package com.jetbrains.lang.dart.highlight;

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

/**
 * @author: Fedor.Korotkov
 */
public class DartSyntaxHighlighterColors {
  public static final String DART_KEYWORD = "DART_KEYWORD";
  public static final String DART_CLASS = "DART_CLASS";
  public static final String DART_BUILTIN = "DART_BUILTIN";
  public static final String DART_INTERFACE = "DART_INTERFACE";
  public static final String DART_FUNCTION = "DART_FUNCTION";
  public static final String DART_STATIC_MEMBER_FUNCTION = "DART_STATIC_MEMBER_FUNCTION";
  public static final String DART_INSTANCE_MEMBER_FUNCTION = "DART_INSTANCE_MEMBER_FUNCTION";
  public static final String DART_INSTANCE_MEMBER_VARIABLE = "DART_INSTANCE_MEMBER_VARIABLE";
  public static final String DART_STATIC_MEMBER_VARIABLE = "DART_STATIC_MEMBER_VARIABLE";
  public static final String DART_LOCAL_VARIABLE = "DART_LOCAL_VARIABLE";
  public static final String DART_PARAMETER = "DART_PARAMETER";
  public static final String DART_LABEL = "DART_LABEL";

  private static final String DART_LINE_COMMENT = "DART_LINE_COMMENT";
  private static final String DART_BLOCK_COMMENT = "DART_BLOCK_COMMENT";
  private static final String DART_DOC_COMMENT = "DART_DOC_COMMENT";

  private static final String DART_NUMBER = "DART_NUMBER";
  private static final String DART_STRING = "DART_STRING";
  private static final String DART_OPERATION_SIGN = "DART_OPERATION_SIGN";
  private static final String DART_PARENTH = "DART_PARENTH";
  private static final String DART_BRACKETS = "DART_BRACKETS";
  private static final String DART_BRACES = "DART_BRACES";
  private static final String DART_COMMA = "DART_COMMA";
  private static final String DART_DOT = "DART_DOT";
  private static final String DART_SEMICOLON = "DART_SEMICOLON";
  private static final String DART_BAD_CHARACTER = "DART_BAD_CHARACTER";

  public static final TextAttributesKey LINE_COMMENT =
    createTextAttributesKey(DART_LINE_COMMENT, DefaultLanguageHighlighterColors.LINE_COMMENT);
  public static final TextAttributesKey BLOCK_COMMENT =
    createTextAttributesKey(DART_BLOCK_COMMENT, DefaultLanguageHighlighterColors.BLOCK_COMMENT);
  public static final TextAttributesKey DOC_COMMENT =
    createTextAttributesKey(DART_DOC_COMMENT, DefaultLanguageHighlighterColors.DOC_COMMENT);
  public static final TextAttributesKey KEYWORD =
    createTextAttributesKey(DART_KEYWORD, DefaultLanguageHighlighterColors.KEYWORD);
  public static final TextAttributesKey BUILTIN =
    createTextAttributesKey(DART_BUILTIN, DefaultLanguageHighlighterColors.KEYWORD);
  public static final TextAttributesKey NUMBER =
    createTextAttributesKey(DART_NUMBER, DefaultLanguageHighlighterColors.NUMBER);
  public static final TextAttributesKey STRING =
    createTextAttributesKey(DART_STRING, DefaultLanguageHighlighterColors.STRING);
  public static final TextAttributesKey OPERATION_SIGN =
    createTextAttributesKey(DART_OPERATION_SIGN, DefaultLanguageHighlighterColors.OPERATION_SIGN);
  public static final TextAttributesKey PARENTHS =
    createTextAttributesKey(DART_PARENTH, DefaultLanguageHighlighterColors.PARENTHESES);
  public static final TextAttributesKey BRACKETS =
    createTextAttributesKey(DART_BRACKETS, DefaultLanguageHighlighterColors.BRACKETS);
  public static final TextAttributesKey BRACES =
    createTextAttributesKey(DART_BRACES, DefaultLanguageHighlighterColors.BRACES);
  public static final TextAttributesKey COMMA = createTextAttributesKey(DART_COMMA, DefaultLanguageHighlighterColors.COMMA);
  public static final TextAttributesKey DOT = createTextAttributesKey(DART_DOT, DefaultLanguageHighlighterColors.DOT);
  public static final TextAttributesKey SEMICOLON =
    createTextAttributesKey(DART_SEMICOLON, DefaultLanguageHighlighterColors.SEMICOLON);
  public static final TextAttributesKey BAD_CHARACTER =
    createTextAttributesKey(DART_BAD_CHARACTER, HighlighterColors.BAD_CHARACTER);
  public static final TextAttributesKey CLASS =
    createTextAttributesKey(DART_CLASS, DefaultLanguageHighlighterColors.CLASS_NAME);
  public static final TextAttributesKey INTERFACE =
    createTextAttributesKey(DART_INTERFACE, DefaultLanguageHighlighterColors.INTERFACE_NAME);
  public static final TextAttributesKey STATIC_MEMBER_FUNCTION =
    createTextAttributesKey(DART_STATIC_MEMBER_FUNCTION, DefaultLanguageHighlighterColors.STATIC_METHOD);
  public static final TextAttributesKey INSTANCE_MEMBER_FUNCTION =
    createTextAttributesKey(DART_INSTANCE_MEMBER_FUNCTION, DefaultLanguageHighlighterColors.INSTANCE_METHOD);
  public static final TextAttributesKey INSTANCE_MEMBER_VARIABLE =
    createTextAttributesKey(DART_INSTANCE_MEMBER_VARIABLE, DefaultLanguageHighlighterColors.INSTANCE_FIELD);
  public static final TextAttributesKey STATIC_MEMBER_VARIABLE =
    createTextAttributesKey(DART_STATIC_MEMBER_VARIABLE, DefaultLanguageHighlighterColors.STATIC_FIELD);
  public static final TextAttributesKey LOCAL_VARIABLE =
    createTextAttributesKey(DART_LOCAL_VARIABLE, DefaultLanguageHighlighterColors.LOCAL_VARIABLE);
  public static final TextAttributesKey FUNCTION =
    createTextAttributesKey(DART_FUNCTION, DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
  public static final TextAttributesKey PARAMETER =
    createTextAttributesKey(DART_PARAMETER, DefaultLanguageHighlighterColors.PARAMETER);
  public static final TextAttributesKey LABEL =
    createTextAttributesKey(DART_LABEL, DefaultLanguageHighlighterColors.LABEL);
}
