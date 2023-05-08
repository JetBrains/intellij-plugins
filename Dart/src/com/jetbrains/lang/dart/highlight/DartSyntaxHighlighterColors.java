// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.highlight;

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public final class DartSyntaxHighlighterColors {
  public static final String DART_ERROR = "DART_ERROR";
  public static final String DART_WARNING = "DART_WARNING";
  public static final String DART_HINT = "DART_HINT";

  public static final String DART_ANNOTATION = "DART_ANNOTATION";
  public static final String DART_CLASS = "DART_CLASS";
  public static final String DART_CONSTRUCTOR = "DART_CONSTRUCTOR";
  public static final String DART_CONSTRUCTOR_TEAR_OFF = "DART_CONSTRUCTOR_TEAR_OFF";

  public static final String DART_DYNAMIC_LOCAL_VARIABLE_DECLARATION = "DART_DYNAMIC_LOCAL_VARIABLE_DECLARATION";
  public static final String DART_DYNAMIC_LOCAL_VARIABLE_REFERENCE = "DART_DYNAMIC_LOCAL_VARIABLE_REFERENCE";
  public static final String DART_DYNAMIC_PARAMETER_DECLARATION = "DART_DYNAMIC_PARAMETER_DECLARATION";
  public static final String DART_DYNAMIC_PARAMETER_REFERENCE = "DART_DYNAMIC_PARAMETER_REFERENCE";

  public static final String DART_ENUM = "DART_ENUM";
  public static final String DART_ENUM_CONSTANT = "DART_ENUM_CONSTANT";
  public static final String DART_EXTENSION = "DART_EXTENSION";
  public static final String DART_FUNCTION_TYPE_ALIAS = "DART_FUNCTION_TYPE_ALIAS";
  public static final String DART_TYPE_ALIAS = "DART_TYPE_ALIAS";

  public static final String DART_IDENTIFIER = "DART_IDENTIFIER";
  public static final String DART_INSTANCE_FIELD_DECLARATION = "DART_INSTANCE_FIELD_DECLARATION";
  public static final String DART_INSTANCE_FIELD_REFERENCE = "DART_INSTANCE_FIELD_REFERENCE";
  public static final String DART_INSTANCE_GETTER_DECLARATION = "DART_INSTANCE_GETTER_DECLARATION";
  public static final String DART_INSTANCE_GETTER_REFERENCE = "DART_INSTANCE_GETTER_REFERENCE";
  public static final String DART_INSTANCE_METHOD_DECLARATION = "DART_INSTANCE_METHOD_DECLARATION";
  public static final String DART_INSTANCE_METHOD_REFERENCE = "DART_INSTANCE_METHOD_REFERENCE";
  public static final String DART_INSTANCE_METHOD_TEAR_OFF = "DART_INSTANCE_METHOD_TEAR_OFF";
  public static final String DART_INSTANCE_SETTER_DECLARATION = "DART_INSTANCE_SETTER_DECLARATION";
  public static final String DART_INSTANCE_SETTER_REFERENCE = "DART_INSTANCE_SETTER_REFERENCE";

  public static final String DART_IMPORT_PREFIX = "DART_IMPORT_PREFIX";
  public static final String DART_KEYWORD = "DART_KEYWORD";
  public static final String DART_LABEL = "DART_LABEL";
  public static final String DART_LIBRARY_NAME = "DART_LIBRARY_NAME";

  public static final String DART_LOCAL_FUNCTION_DECLARATION = "DART_LOCAL_FUNCTION_DECLARATION";
  public static final String DART_LOCAL_FUNCTION_REFERENCE = "DART_LOCAL_FUNCTION_REFERENCE";
  public static final String DART_LOCAL_FUNCTION_TEAR_OFF = "DART_LOCAL_FUNCTION_TEAR_OFF";
  public static final String DART_LOCAL_VARIABLE_DECLARATION = "DART_LOCAL_VARIABLE_DECLARATION";
  public static final String DART_LOCAL_VARIABLE_REFERENCE = "DART_LOCAL_VARIABLE_REFERENCE";

  public static final String DART_MIXIN = "DART_MIXIN";

  public static final String DART_PARAMETER_DECLARATION = "DART_PARAMETER_DECLARATION";
  public static final String DART_PARAMETER_REFERENCE = "DART_PARAMETER_REFERENCE";

  public static final String DART_STATIC_FIELD_DECLARATION = "DART_STATIC_FIELD_DECLARATION";
  public static final String DART_STATIC_GETTER_DECLARATION = "DART_STATIC_GETTER_DECLARATION";
  public static final String DART_STATIC_GETTER_REFERENCE = "DART_STATIC_GETTER_REFERENCE";
  public static final String DART_STATIC_METHOD_DECLARATION = "DART_STATIC_METHOD_DECLARATION";
  public static final String DART_STATIC_METHOD_REFERENCE = "DART_STATIC_METHOD_REFERENCE";
  public static final String DART_STATIC_METHOD_TEAR_OFF = "DART_STATIC_METHOD_TEAR_OFF";
  public static final String DART_STATIC_SETTER_DECLARATION = "DART_STATIC_SETTER_DECLARATION";
  public static final String DART_STATIC_SETTER_REFERENCE = "DART_STATIC_SETTER_REFERENCE";

  public static final String DART_TOP_LEVEL_FUNCTION_DECLARATION = "DART_TOP_LEVEL_FUNCTION_DECLARATION";
  public static final String DART_TOP_LEVEL_FUNCTION_REFERENCE = "DART_TOP_LEVEL_FUNCTION_REFERENCE";
  public static final String DART_TOP_LEVEL_FUNCTION_TEAR_OFF = "DART_TOP_LEVEL_FUNCTION_TEAR_OFF";
  public static final String DART_TOP_LEVEL_GETTER_DECLARATION = "DART_TOP_LEVEL_GETTER_DECLARATION";
  public static final String DART_TOP_LEVEL_GETTER_REFERENCE = "DART_TOP_LEVEL_GETTER_REFERENCE";
  public static final String DART_TOP_LEVEL_SETTER_DECLARATION = "DART_TOP_LEVEL_SETTER_DECLARATION";
  public static final String DART_TOP_LEVEL_SETTER_REFERENCE = "DART_TOP_LEVEL_SETTER_REFERENCE";
  public static final String DART_TOP_LEVEL_VARIABLE_DECLARATION = "DART_TOP_LEVEL_VARIABLE_DECLARATION";

  public static final String DART_TYPE_NAME_DYNAMIC = "DART_TYPE_NAME_DYNAMIC";
  public static final String DART_TYPE_PARAMETER = "DART_TYPE_PARAMETER";
  public static final String DART_UNRESOLVED_INSTANCE_MEMBER_REFERENCE = "DART_UNRESOLVED_INSTANCE_MEMBER_REFERENCE";

  private static final String DART_BLOCK_COMMENT = "DART_BLOCK_COMMENT";
  private static final String DART_DOC_COMMENT = "DART_DOC_COMMENT";
  private static final String DART_LINE_COMMENT = "DART_LINE_COMMENT";

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
  private static final String DART_COLON = "DART_COLON";
  private static final String DART_FAT_ARROW = "DART_FAT_ARROW";
  private static final String DART_BAD_CHARACTER = "DART_BAD_CHARACTER";
  private static final String DART_SYMBOL_LITERAL = "DART_SYMBOL_LITERAL";

  public static final TextAttributesKey ERROR =
    createTextAttributesKey(DART_ERROR, CodeInsightColors.ERRORS_ATTRIBUTES);
  public static final TextAttributesKey WARNING =
    createTextAttributesKey(DART_WARNING, CodeInsightColors.WARNINGS_ATTRIBUTES);
  public static final TextAttributesKey HINT =
    createTextAttributesKey(DART_HINT, CodeInsightColors.WEAK_WARNING_ATTRIBUTES);

  public static final TextAttributesKey BLOCK_COMMENT =
    createTextAttributesKey(DART_BLOCK_COMMENT, DefaultLanguageHighlighterColors.BLOCK_COMMENT);
  public static final TextAttributesKey DOC_COMMENT =
    createTextAttributesKey(DART_DOC_COMMENT, DefaultLanguageHighlighterColors.DOC_COMMENT);
  public static final TextAttributesKey LINE_COMMENT =
    createTextAttributesKey(DART_LINE_COMMENT, DefaultLanguageHighlighterColors.LINE_COMMENT);

  public static final TextAttributesKey NUMBER = createTextAttributesKey(DART_NUMBER, DefaultLanguageHighlighterColors.NUMBER);
  public static final TextAttributesKey STRING = createTextAttributesKey(DART_STRING, DefaultLanguageHighlighterColors.STRING);
  public static final TextAttributesKey VALID_STRING_ESCAPE =
    createTextAttributesKey(DART_VALID_STRING_ESCAPE, DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE);
  public static final TextAttributesKey INVALID_STRING_ESCAPE =
    createTextAttributesKey(DART_INVALID_STRING_ESCAPE, DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE);
  public static final TextAttributesKey OPERATION_SIGN =
    createTextAttributesKey(DART_OPERATION_SIGN, DefaultLanguageHighlighterColors.OPERATION_SIGN);
  public static final TextAttributesKey PARENTHS = createTextAttributesKey(DART_PARENTH, DefaultLanguageHighlighterColors.PARENTHESES);
  public static final TextAttributesKey BRACKETS = createTextAttributesKey(DART_BRACKETS, DefaultLanguageHighlighterColors.BRACKETS);
  public static final TextAttributesKey BRACES = createTextAttributesKey(DART_BRACES, DefaultLanguageHighlighterColors.BRACES);
  public static final TextAttributesKey COMMA = createTextAttributesKey(DART_COMMA, DefaultLanguageHighlighterColors.COMMA);
  public static final TextAttributesKey DOT = createTextAttributesKey(DART_DOT, DefaultLanguageHighlighterColors.DOT);
  public static final TextAttributesKey SEMICOLON = createTextAttributesKey(DART_SEMICOLON, DefaultLanguageHighlighterColors.SEMICOLON);
  public static final TextAttributesKey COLON = createTextAttributesKey(DART_COLON, DefaultLanguageHighlighterColors.OPERATION_SIGN);
  public static final TextAttributesKey FAT_ARROW =
    createTextAttributesKey(DART_FAT_ARROW, DefaultLanguageHighlighterColors.OPERATION_SIGN);
  public static final TextAttributesKey BAD_CHARACTER = createTextAttributesKey(DART_BAD_CHARACTER, HighlighterColors.BAD_CHARACTER);
  public static final TextAttributesKey SYMBOL_LITERAL =
    createTextAttributesKey(DART_SYMBOL_LITERAL, DefaultLanguageHighlighterColors.KEYWORD);

  public static final TextAttributesKey ANNOTATION = createTextAttributesKey(DART_ANNOTATION, DefaultLanguageHighlighterColors.METADATA);
  public static final TextAttributesKey CLASS = createTextAttributesKey(DART_CLASS, DefaultLanguageHighlighterColors.CLASS_NAME);
  public static final TextAttributesKey ENUM = createTextAttributesKey(DART_ENUM, DefaultLanguageHighlighterColors.CLASS_NAME);
  public static final TextAttributesKey ENUM_CONSTANT =
    createTextAttributesKey(DART_ENUM_CONSTANT, DefaultLanguageHighlighterColors.INSTANCE_FIELD);
  public static final TextAttributesKey EXTENSION = createTextAttributesKey(DART_EXTENSION, DefaultLanguageHighlighterColors.CLASS_NAME);
  public static final TextAttributesKey FUNCTION_TYPE_ALIAS =
    createTextAttributesKey(DART_FUNCTION_TYPE_ALIAS, DefaultLanguageHighlighterColors.CLASS_NAME);
  public static final TextAttributesKey TYPE_ALIAS =
    createTextAttributesKey(DART_TYPE_ALIAS, DefaultLanguageHighlighterColors.CLASS_NAME);
  public static final TextAttributesKey IDENTIFIER =
    createTextAttributesKey(DART_IDENTIFIER, DefaultLanguageHighlighterColors.IDENTIFIER);

  public static final TextAttributesKey INSTANCE_FIELD_DECLARATION =
    createTextAttributesKey(DART_INSTANCE_FIELD_DECLARATION, DefaultLanguageHighlighterColors.INSTANCE_FIELD);
  public static final TextAttributesKey INSTANCE_FIELD_REFERENCE =
    createTextAttributesKey(DART_INSTANCE_FIELD_REFERENCE, DefaultLanguageHighlighterColors.INSTANCE_FIELD);
  public static final TextAttributesKey INSTANCE_GETTER_DECLARATION =
    createTextAttributesKey(DART_INSTANCE_GETTER_DECLARATION, INSTANCE_FIELD_DECLARATION);
  public static final TextAttributesKey INSTANCE_GETTER_REFERENCE =
    createTextAttributesKey(DART_INSTANCE_GETTER_REFERENCE, INSTANCE_FIELD_DECLARATION);
  public static final TextAttributesKey INSTANCE_METHOD_DECLARATION =
    createTextAttributesKey(DART_INSTANCE_METHOD_DECLARATION, DefaultLanguageHighlighterColors.INSTANCE_METHOD);
  public static final TextAttributesKey INSTANCE_METHOD_REFERENCE =
    createTextAttributesKey(DART_INSTANCE_METHOD_REFERENCE, DefaultLanguageHighlighterColors.FUNCTION_CALL);
  public static final TextAttributesKey INSTANCE_METHOD_TEAR_OFF =
    createTextAttributesKey(DART_INSTANCE_METHOD_TEAR_OFF, INSTANCE_METHOD_DECLARATION);
  public static final TextAttributesKey INSTANCE_SETTER_DECLARATION =
    createTextAttributesKey(DART_INSTANCE_SETTER_DECLARATION, INSTANCE_FIELD_DECLARATION);
  public static final TextAttributesKey INSTANCE_SETTER_REFERENCE =
    createTextAttributesKey(DART_INSTANCE_SETTER_REFERENCE, INSTANCE_FIELD_DECLARATION);

  public static final TextAttributesKey IMPORT_PREFIX =
    createTextAttributesKey(DART_IMPORT_PREFIX, DefaultLanguageHighlighterColors.IDENTIFIER);
  public static final TextAttributesKey KEYWORD = createTextAttributesKey(DART_KEYWORD, DefaultLanguageHighlighterColors.KEYWORD);
  public static final TextAttributesKey LABEL = createTextAttributesKey(DART_LABEL, DefaultLanguageHighlighterColors.LABEL);
  public static final TextAttributesKey LIBRARY_NAME =
    createTextAttributesKey(DART_LIBRARY_NAME, DefaultLanguageHighlighterColors.IDENTIFIER);
  public static final TextAttributesKey LOCAL_VARIABLE_DECLARATION =
    createTextAttributesKey(DART_LOCAL_VARIABLE_DECLARATION, DefaultLanguageHighlighterColors.LOCAL_VARIABLE);
  public static final TextAttributesKey LOCAL_VARIABLE_REFERENCE =
    createTextAttributesKey(DART_LOCAL_VARIABLE_REFERENCE, DefaultLanguageHighlighterColors.LOCAL_VARIABLE);

  public static final TextAttributesKey MIXIN =
    createTextAttributesKey(DART_MIXIN, DefaultLanguageHighlighterColors.CLASS_NAME);

  public static final TextAttributesKey PARAMETER_DECLARATION =
    createTextAttributesKey(DART_PARAMETER_DECLARATION, DefaultLanguageHighlighterColors.PARAMETER);
  public static final TextAttributesKey PARAMETER_REFERENCE =
    createTextAttributesKey(DART_PARAMETER_REFERENCE, DefaultLanguageHighlighterColors.PARAMETER);

  public static final TextAttributesKey STATIC_FIELD_DECLARATION =
    createTextAttributesKey(DART_STATIC_FIELD_DECLARATION, DefaultLanguageHighlighterColors.STATIC_FIELD);
  public static final TextAttributesKey STATIC_GETTER_DECLARATION =
    createTextAttributesKey(DART_STATIC_GETTER_DECLARATION, STATIC_FIELD_DECLARATION);
  public static final TextAttributesKey STATIC_GETTER_REFERENCE =
    createTextAttributesKey(DART_STATIC_GETTER_REFERENCE, STATIC_FIELD_DECLARATION);
  public static final TextAttributesKey STATIC_METHOD_DECLARATION =
    createTextAttributesKey(DART_STATIC_METHOD_DECLARATION, DefaultLanguageHighlighterColors.STATIC_METHOD);
  public static final TextAttributesKey STATIC_METHOD_REFERENCE =
    createTextAttributesKey(DART_STATIC_METHOD_REFERENCE, DefaultLanguageHighlighterColors.STATIC_METHOD);
  public static final TextAttributesKey STATIC_METHOD_TEAR_OFF =
    createTextAttributesKey(DART_STATIC_METHOD_TEAR_OFF, STATIC_METHOD_DECLARATION);
  public static final TextAttributesKey STATIC_SETTER_DECLARATION =
    createTextAttributesKey(DART_STATIC_SETTER_DECLARATION, STATIC_FIELD_DECLARATION);
  public static final TextAttributesKey STATIC_SETTER_REFERENCE =
    createTextAttributesKey(DART_STATIC_SETTER_REFERENCE, STATIC_FIELD_DECLARATION);

  public static final TextAttributesKey TOP_LEVEL_VARIABLE_DECLARATION =
    createTextAttributesKey(DART_TOP_LEVEL_VARIABLE_DECLARATION, DefaultLanguageHighlighterColors.GLOBAL_VARIABLE);
  public static final TextAttributesKey TOP_LEVEL_FUNCTION_DECLARATION =
    createTextAttributesKey(DART_TOP_LEVEL_FUNCTION_DECLARATION, DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
  public static final TextAttributesKey TOP_LEVEL_FUNCTION_REFERENCE =
    createTextAttributesKey(DART_TOP_LEVEL_FUNCTION_REFERENCE, DefaultLanguageHighlighterColors.FUNCTION_CALL);
  public static final TextAttributesKey TOP_LEVEL_FUNCTION_TEAR_OFF =
    createTextAttributesKey(DART_TOP_LEVEL_FUNCTION_TEAR_OFF, TOP_LEVEL_FUNCTION_DECLARATION);
  public static final TextAttributesKey TOP_LEVEL_GETTER_DECLARATION =
    createTextAttributesKey(DART_TOP_LEVEL_GETTER_DECLARATION, TOP_LEVEL_VARIABLE_DECLARATION);
  public static final TextAttributesKey TOP_LEVEL_GETTER_REFERENCE =
    createTextAttributesKey(DART_TOP_LEVEL_GETTER_REFERENCE, TOP_LEVEL_VARIABLE_DECLARATION);
  public static final TextAttributesKey TOP_LEVEL_SETTER_DECLARATION =
    createTextAttributesKey(DART_TOP_LEVEL_SETTER_DECLARATION, TOP_LEVEL_VARIABLE_DECLARATION);
  public static final TextAttributesKey TOP_LEVEL_SETTER_REFERENCE =
    createTextAttributesKey(DART_TOP_LEVEL_SETTER_REFERENCE, TOP_LEVEL_VARIABLE_DECLARATION);

  public static final TextAttributesKey TYPE_NAME_DYNAMIC =
    createTextAttributesKey(DART_TYPE_NAME_DYNAMIC, DefaultLanguageHighlighterColors.KEYWORD);
  public static final TextAttributesKey TYPE_PARAMETER =
    createTextAttributesKey(DART_TYPE_PARAMETER, DefaultLanguageHighlighterColors.CLASS_NAME);
  public static final TextAttributesKey UNRESOLVED_INSTANCE_MEMBER_REFERENCE =
    createTextAttributesKey(DART_UNRESOLVED_INSTANCE_MEMBER_REFERENCE, DefaultLanguageHighlighterColors.IDENTIFIER);

  public static final TextAttributesKey CONSTRUCTOR =
    createTextAttributesKey(DART_CONSTRUCTOR, INSTANCE_METHOD_DECLARATION);
  public static final TextAttributesKey CONSTRUCTOR_TEAR_OFF =
    createTextAttributesKey(DART_CONSTRUCTOR_TEAR_OFF, CONSTRUCTOR);

  public static final TextAttributesKey DYNAMIC_LOCAL_VARIABLE_DECLARATION =
    createTextAttributesKey(DART_DYNAMIC_LOCAL_VARIABLE_DECLARATION, LOCAL_VARIABLE_DECLARATION);
  public static final TextAttributesKey DYNAMIC_LOCAL_VARIABLE_REFERENCE =
    createTextAttributesKey(DART_DYNAMIC_LOCAL_VARIABLE_REFERENCE, LOCAL_VARIABLE_REFERENCE);
  public static final TextAttributesKey DYNAMIC_PARAMETER_DECLARATION =
    createTextAttributesKey(DART_DYNAMIC_PARAMETER_DECLARATION, PARAMETER_DECLARATION);
  public static final TextAttributesKey DYNAMIC_PARAMETER_REFERENCE =
    createTextAttributesKey(DART_DYNAMIC_PARAMETER_REFERENCE, PARAMETER_REFERENCE);

  public static final TextAttributesKey LOCAL_FUNCTION_DECLARATION =
    createTextAttributesKey(DART_LOCAL_FUNCTION_DECLARATION, LOCAL_VARIABLE_DECLARATION);
  public static final TextAttributesKey LOCAL_FUNCTION_REFERENCE =
    createTextAttributesKey(DART_LOCAL_FUNCTION_REFERENCE, LOCAL_VARIABLE_REFERENCE);
  public static final TextAttributesKey LOCAL_FUNCTION_TEAR_OFF =
    createTextAttributesKey(DART_LOCAL_FUNCTION_TEAR_OFF, LOCAL_FUNCTION_DECLARATION);
}
