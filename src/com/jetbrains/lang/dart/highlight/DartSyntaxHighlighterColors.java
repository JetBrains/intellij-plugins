package com.jetbrains.lang.dart.highlight;

import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.SyntaxHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.psi.tree.IElementType;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

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
    createTextAttributesKey(DART_LINE_COMMENT, SyntaxHighlighterColors.LINE_COMMENT.getDefaultAttributes());
  public static final TextAttributesKey BLOCK_COMMENT =
    createTextAttributesKey(DART_BLOCK_COMMENT, SyntaxHighlighterColors.JAVA_BLOCK_COMMENT.getDefaultAttributes());
  public static final TextAttributesKey DOC_COMMENT =
    createTextAttributesKey(DART_DOC_COMMENT, SyntaxHighlighterColors.DOC_COMMENT.getDefaultAttributes());
  public static final TextAttributesKey KEYWORD =
    createTextAttributesKey(DART_KEYWORD, SyntaxHighlighterColors.KEYWORD.getDefaultAttributes());
  public static final TextAttributesKey BUILTIN =
    createTextAttributesKey(DART_BUILTIN, SyntaxHighlighterColors.KEYWORD.getDefaultAttributes());
  public static final TextAttributesKey NUMBER =
    createTextAttributesKey(DART_NUMBER, SyntaxHighlighterColors.NUMBER.getDefaultAttributes());
  public static final TextAttributesKey STRING =
    createTextAttributesKey(DART_STRING, SyntaxHighlighterColors.STRING.getDefaultAttributes());
  public static final TextAttributesKey OPERATION_SIGN =
    createTextAttributesKey(DART_OPERATION_SIGN, SyntaxHighlighterColors.OPERATION_SIGN.getDefaultAttributes());
  public static final TextAttributesKey PARENTHS =
    createTextAttributesKey(DART_PARENTH, SyntaxHighlighterColors.PARENTHS.getDefaultAttributes());
  public static final TextAttributesKey BRACKETS =
    createTextAttributesKey(DART_BRACKETS, SyntaxHighlighterColors.BRACKETS.getDefaultAttributes());
  public static final TextAttributesKey BRACES =
    createTextAttributesKey(DART_BRACES, SyntaxHighlighterColors.BRACES.getDefaultAttributes());
  public static final TextAttributesKey COMMA = createTextAttributesKey(DART_COMMA, SyntaxHighlighterColors.COMMA.getDefaultAttributes());
  public static final TextAttributesKey DOT = createTextAttributesKey(DART_DOT, SyntaxHighlighterColors.DOT.getDefaultAttributes());
  public static final TextAttributesKey SEMICOLON =
    createTextAttributesKey(DART_SEMICOLON, SyntaxHighlighterColors.JAVA_SEMICOLON.getDefaultAttributes());
  public static final TextAttributesKey BAD_CHARACTER =
    createTextAttributesKey(DART_BAD_CHARACTER, HighlighterColors.BAD_CHARACTER.getDefaultAttributes());
  public static final TextAttributesKey CLASS =
    createTextAttributesKey(DART_CLASS, HighlightInfoType.CLASS_NAME.getAttributesKey().getDefaultAttributes());
  public static final TextAttributesKey INTERFACE =
    createTextAttributesKey(DART_INTERFACE, HighlightInfoType.INTERFACE_NAME.getAttributesKey().getDefaultAttributes());
  public static final TextAttributesKey STATIC_MEMBER_FUNCTION =
    createTextAttributesKey(DART_STATIC_MEMBER_FUNCTION, HighlightInfoType.STATIC_METHOD.getAttributesKey().getDefaultAttributes());
  public static final TextAttributesKey INSTANCE_MEMBER_FUNCTION =
    createTextAttributesKey(DART_INSTANCE_MEMBER_FUNCTION, new TextAttributes(new Color(0x7a, 0x7a, 43), Color.white, null, null, 0));
  public static final TextAttributesKey INSTANCE_MEMBER_VARIABLE =
    createTextAttributesKey(DART_INSTANCE_MEMBER_VARIABLE, HighlightInfoType.INSTANCE_FIELD.getAttributesKey().getDefaultAttributes());
  public static final TextAttributesKey STATIC_MEMBER_VARIABLE =
    createTextAttributesKey(DART_STATIC_MEMBER_VARIABLE, HighlightInfoType.STATIC_FIELD.getAttributesKey().getDefaultAttributes());
  public static final TextAttributesKey LOCAL_VARIABLE =
    createTextAttributesKey(DART_LOCAL_VARIABLE, new TextAttributes(new Color(69, 131, 131), Color.white, null, null, 0));
  public static final TextAttributesKey FUNCTION =
    createTextAttributesKey(DART_FUNCTION, new TextAttributes(new Color(69, 131, 131), Color.white, null, null, 0));
  public static final TextAttributesKey PARAMETER =
    createTextAttributesKey(DART_PARAMETER, new TextAttributes(Color.black, Color.white, Color.black, EffectType.LINE_UNDERSCORE, 0));
  public static final TextAttributesKey LABEL =
    createTextAttributesKey(DART_LABEL, HighlightInfoType.LOCAL_VARIABLE.getAttributesKey().getDefaultAttributes());
}
