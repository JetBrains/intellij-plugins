package com.jetbrains.lang.dart.highlight;

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class DartSyntaxHighlighterColors {
  public static final String DART_KEYWORD = "DART_KEYWORD";
  public static final String DART_BUILTIN = "DART_BUILTIN";
  public static final String DART_CLASS = "DART_CLASS";
  public static final String DART_ENUM = "DART_ENUM";
  public static final String DART_ENUM_CONSTANT = "DART_ENUM_CONSTANT";
  public static final String DART_FUNCTION = "DART_FUNCTION";
  public static final String DART_FUNCTION_TYPE_ALIAS = "DART_FUNCTION_TYPE_ALIAS";
  public static final String DART_STATIC_MEMBER_FUNCTION = "DART_STATIC_MEMBER_FUNCTION";
  public static final String DART_STATIC_MEMBER_FUNCTION_CALL = "DART_STATIC_MEMBER_FUNCTION_CALL";
  public static final String DART_INSTANCE_MEMBER_FUNCTION = "DART_INSTANCE_MEMBER_FUNCTION";
  public static final String DART_INSTANCE_MEMBER_FUNCTION_CALL = "DART_INSTANCE_MEMBER_FUNCTION_CALL";
  public static final String DART_INHERITED_MEMBER_FUNCTION_CALL = "DART_INHERITED_MEMBER_FUNCTION_CALL";
  public static final String DART_ABSTRACT_MEMBER_FUNCTION_CALL = "DART_ABSTRACT_MEMBER_FUNCTION_CALL";
  public static final String DART_INSTANCE_MEMBER_VARIABLE = "DART_INSTANCE_MEMBER_VARIABLE";
  public static final String DART_INSTANCE_MEMBER_VARIABLE_ACCESS = "DART_INSTANCE_MEMBER_VARIABLE_ACCESS";
  public static final String DART_STATIC_MEMBER_VARIABLE = "DART_STATIC_MEMBER_VARIABLE";
  public static final String DART_STATIC_MEMBER_VARIABLE_ACCESS = "DART_STATIC_MEMBER_VARIABLE_ACCESS";
  public static final String DART_LOCAL_VARIABLE = "DART_LOCAL_VARIABLE";
  public static final String DART_LOCAL_VARIABLE_ACCESS = "DART_LOCAL_VARIABLE_ACCESS";
  public static final String DART_PARAMETER = "DART_PARAMETER";
  public static final String DART_LABEL = "DART_LABEL";
  public static final String DART_METADATA = "DART_METADATA";
  public static final String DART_CONSTRUCTOR_CALL = "DART_CONSTRUCTOR_CALL";
  public static final String DART_CONSTRUCTOR_DECLARATION = "DART_CONSTRUCTOR_DECLARATION";
  public static final String DART_TOP_LEVEL_FUNCTION_DECLARATION = "DART_TOP_LEVEL_FUNCTION_DECLARATION";
  public static final String DART_TOP_LEVEL_FUNCTION_CALL = "DART_TOP_LEVEL_FUNCTION_CALL";
  public static final String DART_TOP_LEVEL_VARIABLE_DECLARATION = "DART_TOP_LEVEL_VARIABLE_DECLARATION";
  public static final String DART_TOP_LEVEL_VARIABLE_ACCESS = "DART_TOP_LEVEL_VARIABLE_ACCESS";
  public static final String DART_TYPE_PARAMETER = "DART_TYPE_PARAMETER";
  public static final String DART_VARIABLE_OF_DYNAMIC_TYPE = "DART_VARIABLE_OF_DYNAMIC_TYPE";
  public static final String DART_GETTER_DECLARATION = "DART_GETTER_DECLARATION";
  public static final String DART_GETTER_DECLARATION_STATIC = "DART_GETTER_DECLARATION_STATIC";
  public static final String DART_SETTER_DECLARATION = "DART_SETTER_DECLARATION";
  public static final String DART_SETTER_DECLARATION_STATIC = "DART_SETTER_DECLARATION_STATIC";
  public static final String DART_IMPORT_PREFIX = "DART_IMPORT_PREFIX";

  private static final String DART_LINE_COMMENT = "DART_LINE_COMMENT";
  private static final String DART_BLOCK_COMMENT = "DART_BLOCK_COMMENT";
  private static final String DART_DOC_COMMENT = "DART_DOC_COMMENT";

  private static final String DART_NUMBER = "DART_NUMBER";
  private static final String DART_STRING = "DART_STRING";
  private static final String DART_VALID_STRING_ESCAPE = "DART_VALID_STRING_ESCAPE";
  private static final String DART_INVALID_STRING_ESCAPE = "DART_INVALID_STRING_ESCAPE";
  private static final String DART_OPERATION_SIGN = "DART_OPERATION_SIGN";
  private static final String DART_PARENTH = "DART_PARENTH";
  private static final String DART_BRACKETS = "DART_BRACKETS";
  private static final String DART_BRACES = "DART_BRACES";
  private static final String DART_COMMA = "DART_COMMA";
  private static final String DART_DOT = "DART_DOT";
  private static final String DART_SEMICOLON = "DART_SEMICOLON";
  private static final String DART_BAD_CHARACTER = "DART_BAD_CHARACTER";
  private static final String DART_SYMBOL_LITERAL = "DART_SYMBOL_LITERAL";


  public static final TextAttributesKey LINE_COMMENT =
    createTextAttributesKey(DART_LINE_COMMENT, DefaultLanguageHighlighterColors.LINE_COMMENT);
  public static final TextAttributesKey BLOCK_COMMENT =
    createTextAttributesKey(DART_BLOCK_COMMENT, DefaultLanguageHighlighterColors.BLOCK_COMMENT);
  public static final TextAttributesKey DOC_COMMENT =
    createTextAttributesKey(DART_DOC_COMMENT, DefaultLanguageHighlighterColors.DOC_COMMENT);
  public static final TextAttributesKey KEYWORD = createTextAttributesKey(DART_KEYWORD, DefaultLanguageHighlighterColors.KEYWORD);
  public static final TextAttributesKey BUILTIN = createTextAttributesKey(DART_BUILTIN, DefaultLanguageHighlighterColors.KEYWORD);
  public static final TextAttributesKey NUMBER = createTextAttributesKey(DART_NUMBER, DefaultLanguageHighlighterColors.NUMBER);
  public static final TextAttributesKey STRING = createTextAttributesKey(DART_STRING, DefaultLanguageHighlighterColors.STRING);
  public static final TextAttributesKey VALID_STRING_ESCAPE =
    createTextAttributesKey(DART_VALID_STRING_ESCAPE, DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE);
  public static final TextAttributesKey INVALID_STRING_ESCAPE =
    createTextAttributesKey(DART_INVALID_STRING_ESCAPE, DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE);
  public static final TextAttributesKey SYMBOL_LITERAL =
    createTextAttributesKey(DART_SYMBOL_LITERAL, DefaultLanguageHighlighterColors.KEYWORD);
  public static final TextAttributesKey OPERATION_SIGN =
    createTextAttributesKey(DART_OPERATION_SIGN, DefaultLanguageHighlighterColors.OPERATION_SIGN);
  public static final TextAttributesKey PARENTHS = createTextAttributesKey(DART_PARENTH, DefaultLanguageHighlighterColors.PARENTHESES);
  public static final TextAttributesKey BRACKETS = createTextAttributesKey(DART_BRACKETS, DefaultLanguageHighlighterColors.BRACKETS);
  public static final TextAttributesKey BRACES = createTextAttributesKey(DART_BRACES, DefaultLanguageHighlighterColors.BRACES);
  public static final TextAttributesKey COMMA = createTextAttributesKey(DART_COMMA, DefaultLanguageHighlighterColors.COMMA);
  public static final TextAttributesKey DOT = createTextAttributesKey(DART_DOT, DefaultLanguageHighlighterColors.DOT);
  public static final TextAttributesKey SEMICOLON = createTextAttributesKey(DART_SEMICOLON, DefaultLanguageHighlighterColors.SEMICOLON);
  public static final TextAttributesKey BAD_CHARACTER = createTextAttributesKey(DART_BAD_CHARACTER, HighlighterColors.BAD_CHARACTER);
  public static final TextAttributesKey CLASS = createTextAttributesKey(DART_CLASS, DefaultLanguageHighlighterColors.CLASS_NAME);
  public static final TextAttributesKey STATIC_MEMBER_FUNCTION =
    createTextAttributesKey(DART_STATIC_MEMBER_FUNCTION, DefaultLanguageHighlighterColors.STATIC_METHOD);
  public static final TextAttributesKey STATIC_MEMBER_FUNCTION_CALL =
    createTextAttributesKey(DART_STATIC_MEMBER_FUNCTION_CALL, DefaultLanguageHighlighterColors.STATIC_METHOD);
  public static final TextAttributesKey INSTANCE_MEMBER_FUNCTION =
    createTextAttributesKey(DART_INSTANCE_MEMBER_FUNCTION, DefaultLanguageHighlighterColors.INSTANCE_METHOD);
  public static final TextAttributesKey INSTANCE_MEMBER_FUNCTION_CALL =
    createTextAttributesKey(DART_INSTANCE_MEMBER_FUNCTION_CALL, DefaultLanguageHighlighterColors.FUNCTION_CALL);
  public static final TextAttributesKey INHERITED_MEMBER_FUNCTION_CALL =
    createTextAttributesKey(DART_INHERITED_MEMBER_FUNCTION_CALL, DefaultLanguageHighlighterColors.FUNCTION_CALL);
  public static final TextAttributesKey ABSTRACT_MEMBER_FUNCTION_CALL =
    createTextAttributesKey(DART_ABSTRACT_MEMBER_FUNCTION_CALL, DefaultLanguageHighlighterColors.FUNCTION_CALL);
  public static final TextAttributesKey INSTANCE_MEMBER_VARIABLE =
    createTextAttributesKey(DART_INSTANCE_MEMBER_VARIABLE, DefaultLanguageHighlighterColors.INSTANCE_FIELD);
  public static final TextAttributesKey INSTANCE_MEMBER_VARIABLE_ACCESS =
    createTextAttributesKey(DART_INSTANCE_MEMBER_VARIABLE_ACCESS, DefaultLanguageHighlighterColors.INSTANCE_FIELD);
  public static final TextAttributesKey STATIC_MEMBER_VARIABLE =
    createTextAttributesKey(DART_STATIC_MEMBER_VARIABLE, DefaultLanguageHighlighterColors.STATIC_FIELD);
  public static final TextAttributesKey STATIC_MEMBER_VARIABLE_ACCESS =
    createTextAttributesKey(DART_STATIC_MEMBER_VARIABLE_ACCESS, DefaultLanguageHighlighterColors.STATIC_FIELD);
  public static final TextAttributesKey LOCAL_VARIABLE =
    createTextAttributesKey(DART_LOCAL_VARIABLE, DefaultLanguageHighlighterColors.LOCAL_VARIABLE);
  public static final TextAttributesKey LOCAL_VARIABLE_ACCESS =
    createTextAttributesKey(DART_LOCAL_VARIABLE_ACCESS, DefaultLanguageHighlighterColors.LOCAL_VARIABLE);
  public static final TextAttributesKey ENUM = createTextAttributesKey(DART_ENUM, DefaultLanguageHighlighterColors.CLASS_NAME);
  public static final TextAttributesKey ENUM_CONSTANT =
    createTextAttributesKey(DART_ENUM_CONSTANT, DefaultLanguageHighlighterColors.INSTANCE_FIELD);
  public static final TextAttributesKey FUNCTION =
    createTextAttributesKey(DART_FUNCTION, DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
  public static final TextAttributesKey FUNCTION_TYPE_ALIAS =
    createTextAttributesKey(DART_FUNCTION_TYPE_ALIAS, DefaultLanguageHighlighterColors.CLASS_NAME);
  public static final TextAttributesKey PARAMETER = createTextAttributesKey(DART_PARAMETER, DefaultLanguageHighlighterColors.PARAMETER);
  public static final TextAttributesKey LABEL = createTextAttributesKey(DART_LABEL, DefaultLanguageHighlighterColors.LABEL);
  public static final TextAttributesKey METADATA = createTextAttributesKey(DART_METADATA, DefaultLanguageHighlighterColors.METADATA);
  public static final TextAttributesKey CONSTRUCTOR_CALL =
    createTextAttributesKey(DART_CONSTRUCTOR_CALL, DefaultLanguageHighlighterColors.FUNCTION_CALL);
  public static final TextAttributesKey CONSTRUCTOR_DECLARATION =
    createTextAttributesKey(DART_CONSTRUCTOR_DECLARATION, DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
  public static final TextAttributesKey TOP_LEVEL_FUNCTION_DECLARATION =
    createTextAttributesKey(DART_TOP_LEVEL_FUNCTION_DECLARATION, DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
  public static final TextAttributesKey TOP_LEVEL_FUNCTION_CALL =
    createTextAttributesKey(DART_TOP_LEVEL_FUNCTION_CALL, DefaultLanguageHighlighterColors.FUNCTION_CALL);
  public static final TextAttributesKey TOP_LEVEL_VARIABLE_DECLARATION =
    createTextAttributesKey(DART_TOP_LEVEL_VARIABLE_DECLARATION, DefaultLanguageHighlighterColors.GLOBAL_VARIABLE);
  public static final TextAttributesKey TOP_LEVEL_VARIABLE_ACCESS =
    createTextAttributesKey(DART_TOP_LEVEL_VARIABLE_ACCESS, DefaultLanguageHighlighterColors.GLOBAL_VARIABLE);
  public static final TextAttributesKey TYPE_PARAMETER =
    createTextAttributesKey(DART_TYPE_PARAMETER, DefaultLanguageHighlighterColors.CLASS_NAME);
  public static final TextAttributesKey VARIABLE_OF_DYNAMIC_TYPE = createTextAttributesKey(DART_VARIABLE_OF_DYNAMIC_TYPE, LOCAL_VARIABLE);
  public static final TextAttributesKey GETTER_DECLARATION = createTextAttributesKey(DART_GETTER_DECLARATION, INSTANCE_MEMBER_FUNCTION);
  public static final TextAttributesKey GETTER_DECLARATION_STATIC =
    createTextAttributesKey(DART_GETTER_DECLARATION_STATIC, STATIC_MEMBER_FUNCTION);
  public static final TextAttributesKey SETTER_DECLARATION = createTextAttributesKey(DART_SETTER_DECLARATION, INSTANCE_MEMBER_FUNCTION);
  public static final TextAttributesKey SETTER_DECLARATION_STATIC =
    createTextAttributesKey(DART_SETTER_DECLARATION_STATIC, STATIC_MEMBER_FUNCTION);
  public static final TextAttributesKey IMPORT_PREFIX =
    createTextAttributesKey(DART_IMPORT_PREFIX, DefaultLanguageHighlighterColors.IDENTIFIER);
}
