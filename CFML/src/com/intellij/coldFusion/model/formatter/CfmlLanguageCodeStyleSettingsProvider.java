/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
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
package com.intellij.coldFusion.model.formatter;

/**
 * Created by IntelliJ IDEA.
 * User: Nadya.Zabrodina
 */

import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.coldFusion.model.files.CfmlFileType;
import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.*;
import org.jetbrains.annotations.NotNull;

public class CfmlLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {

  @NotNull
  @Override
  public Language getLanguage() {
    return CfmlLanguage.INSTANCE;
  }

  @Override
  public String getCodeSample(@NotNull SettingsType settingsType) {
    if (settingsType == SettingsType.SPACING_SETTINGS) return SPACING_CODE_SAMPLE;
    if (settingsType == SettingsType.BLANK_LINES_SETTINGS) return BLANK_LINE_CODE_SAMPLE;
    if (settingsType == SettingsType.WRAPPING_AND_BRACES_SETTINGS) return WRAPPING_CODE_SAMPLE;

    return GENERAL_CODE_SAMPLE;
  }

  @Override
  public void customizeSettings(@NotNull CodeStyleSettingsCustomizable consumer, @NotNull SettingsType settingsType) {
    if (settingsType == SettingsType.SPACING_SETTINGS) {
      consumer.showStandardOptions("SPACE_BEFORE_METHOD_CALL_PARENTHESES",
                                   "SPACE_BEFORE_METHOD_PARENTHESES",
                                   "SPACE_BEFORE_IF_PARENTHESES",
                                   "SPACE_BEFORE_WHILE_PARENTHESES",
                                   "SPACE_BEFORE_FOR_PARENTHESES",
                                   "SPACE_BEFORE_CATCH_PARENTHESES",
                                   "SPACE_BEFORE_SWITCH_PARENTHESES",
                                   "SPACE_AROUND_ASSIGNMENT_OPERATORS",
                                   "SPACE_AROUND_LOGICAL_OPERATORS",
                                   "SPACE_AROUND_EQUALITY_OPERATORS",
                                   "SPACE_AROUND_RELATIONAL_OPERATORS",
                                   "SPACE_AROUND_ADDITIVE_OPERATORS",
                                   "SPACE_AROUND_MULTIPLICATIVE_OPERATORS",
                                   "SPACE_BEFORE_METHOD_LBRACE",
                                   "SPACE_BEFORE_IF_LBRACE",
                                   "SPACE_BEFORE_ELSE_LBRACE",
                                   "SPACE_BEFORE_WHILE_LBRACE",
                                   "SPACE_BEFORE_FOR_LBRACE",
                                   "SPACE_BEFORE_SWITCH_LBRACE",
                                   "SPACE_BEFORE_TRY_LBRACE",
                                   "SPACE_BEFORE_CATCH_LBRACE",
                                   "SPACE_BEFORE_WHILE_KEYWORD",
                                   "SPACE_BEFORE_ELSE_KEYWORD",
                                   "SPACE_BEFORE_CATCH_KEYWORD",
                                   "SPACE_WITHIN_METHOD_CALL_PARENTHESES",
                                   "SPACE_WITHIN_METHOD_PARENTHESES",
                                   "SPACE_WITHIN_IF_PARENTHESES",
                                   "SPACE_WITHIN_WHILE_PARENTHESES",
                                   "SPACE_WITHIN_FOR_PARENTHESES",
                                   "SPACE_WITHIN_CATCH_PARENTHESES",
                                   "SPACE_WITHIN_SWITCH_PARENTHESES",
                                   "SPACE_BEFORE_QUEST",
                                   "SPACE_AFTER_QUEST",
                                   "SPACE_BEFORE_COLON",
                                   "SPACE_AFTER_COLON",
                                   "SPACE_AFTER_COMMA",
                                   "SPACE_BEFORE_COMMA",
                                   "SPACE_AFTER_SEMICOLON",
                                   "SPACE_BEFORE_SEMICOLON",
                                   "SPACE_AROUND_UNARY_OPERATOR"
      );
      consumer.showCustomOption(CfmlCodeStyleSettings.class, "CONCAT_SPACES", "Concatenation (&)",
                                CodeStyleSettingsCustomizable.SPACES_AROUND_OPERATORS);
    }
    else if (settingsType == SettingsType.BLANK_LINES_SETTINGS) {
      consumer.showStandardOptions("KEEP_BLANK_LINES_IN_CODE");
    }
    else if (settingsType == SettingsType.WRAPPING_AND_BRACES_SETTINGS) {
      consumer.showStandardOptions("KEEP_LINE_BREAKS",
                                   "KEEP_FIRST_COLUMN_COMMENT",
                                   "BRACE_STYLE",
                                   "METHOD_BRACE_STYLE",
                                   "CALL_PARAMETERS_WRAP",
                                   "CALL_PARAMETERS_LPAREN_ON_NEXT_LINE",
                                   "CALL_PARAMETERS_RPAREN_ON_NEXT_LINE",
                                   "METHOD_PARAMETERS_WRAP",
                                   "METHOD_PARAMETERS_LPAREN_ON_NEXT_LINE",
                                   "METHOD_PARAMETERS_RPAREN_ON_NEXT_LINE",
                                   "ELSE_ON_NEW_LINE",
                                   "WHILE_ON_NEW_LINE",
                                   "CATCH_ON_NEW_LINE",
                                   "ALIGN_MULTILINE_PARAMETERS",
                                   "ALIGN_MULTILINE_PARAMETERS_IN_CALLS",
                                   "ALIGN_MULTILINE_FOR",
                                   "ALIGN_MULTILINE_BINARY_OPERATION",
                                   "FOR_STATEMENT_WRAP",
                                   "FOR_STATEMENT_LPAREN_ON_NEXT_LINE",
                                   "FOR_STATEMENT_RPAREN_ON_NEXT_LINE",
                                   "BINARY_OPERATION_WRAP",
                                   "BINARY_OPERATION_SIGN_ON_NEXT_LINE",
                                   "TERNARY_OPERATION_WRAP",
                                   "TERNARY_OPERATION_SIGNS_ON_NEXT_LINE",
                                   "ASSIGNMENT_WRAP",
                                   "PLACE_ASSIGNMENT_SIGN_ON_NEXT_LINE",
                                   "PARENTHESES_EXPRESSION_LPAREN_WRAP",
                                   "PARENTHESES_EXPRESSION_RPAREN_WRAP",
                                   "ALIGN_MULTILINE_TERNARY_OPERATION",
                                   "SPECIAL_ELSE_IF_TREATMENT");
    }
  }

  @Override
  public CommonCodeStyleSettings getDefaultCommonSettings() {
    CommonCodeStyleSettings commonSettings = new CommonCodeStyleSettings(CfmlLanguage.INSTANCE);
    commonSettings.CLASS_BRACE_STYLE = CommonCodeStyleSettings.NEXT_LINE;
    commonSettings.METHOD_BRACE_STYLE = CommonCodeStyleSettings.NEXT_LINE;
    commonSettings.SPECIAL_ELSE_IF_TREATMENT = false;
    commonSettings.SPACE_AFTER_TYPE_CAST = false;
    return commonSettings;
  }

  public static class PearCodeStyle extends PredefinedCodeStyle {

    public PearCodeStyle() {
      super("PEAR", CfmlLanguage.INSTANCE);
    }

    protected PearCodeStyle(String name) {
      super(name, CfmlLanguage.INSTANCE);
    }

    @Override
    public void apply(CodeStyleSettings settings) {
      CodeStyleSettings.IndentOptions indentOptions = settings.getIndentOptions(CfmlFileType.INSTANCE);
      indentOptions.INDENT_SIZE = 4;
      indentOptions.CONTINUATION_INDENT_SIZE = 4;
      indentOptions.USE_TAB_CHARACTER = false;
      indentOptions.TAB_SIZE = 4;
      settings.RIGHT_MARGIN = 80;

      CfmlCodeStyleSettings cfmlSettings = settings.getCustomSettings(CfmlCodeStyleSettings.class);

      cfmlSettings.INDENT_CODE_IN_CFML_TAGS = true;
      cfmlSettings.ALIGN_CFMLDOC_PARAM_NAMES = true;
      cfmlSettings.ALIGN_CFMLDOC_COMMENTS = true;
      cfmlSettings.CFMLDOC_BLANK_LINE_BEFORE_TAGS = true;
      cfmlSettings.CFMLDOC_BLANK_LINES_AROUND_PARAMETERS = true;

      CommonCodeStyleSettings commonSettings = settings.getCommonSettings(getLanguage());
      commonSettings.BRACE_STYLE = CodeStyleSettings.END_OF_LINE;
      commonSettings.METHOD_BRACE_STYLE = CodeStyleSettings.NEXT_LINE;
      commonSettings.DO_NOT_INDENT_TOP_LEVEL_CLASS_MEMBERS = false;
      commonSettings.ELSE_ON_NEW_LINE = false;
      commonSettings.WHILE_ON_NEW_LINE = false;
      commonSettings.CATCH_ON_NEW_LINE = false;

      commonSettings.ALIGN_MULTILINE_PARAMETERS = false;
      commonSettings.ALIGN_MULTILINE_PARAMETERS_IN_CALLS = false;
      commonSettings.ALIGN_MULTILINE_FOR = false;
      commonSettings.ALIGN_MULTILINE_BINARY_OPERATION = false;
      commonSettings.ALIGN_MULTILINE_ASSIGNMENT = false;
      commonSettings.ALIGN_MULTILINE_TERNARY_OPERATION = false;
      commonSettings.ALIGN_THROWS_KEYWORD = false;
      commonSettings.ALIGN_MULTILINE_METHOD_BRACKETS = false;
      commonSettings.ALIGN_MULTILINE_PARENTHESIZED_EXPRESSION = false;

      commonSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS = true;
      commonSettings.SPACE_AROUND_LOGICAL_OPERATORS = true;
      commonSettings.SPACE_AROUND_EQUALITY_OPERATORS = true;
      commonSettings.SPACE_AROUND_RELATIONAL_OPERATORS = true;
      commonSettings.SPACE_AROUND_BITWISE_OPERATORS = true;
      commonSettings.SPACE_AROUND_ADDITIVE_OPERATORS = true;
      commonSettings.SPACE_AROUND_MULTIPLICATIVE_OPERATORS = true;
      commonSettings.SPACE_AROUND_SHIFT_OPERATORS = true;
      commonSettings.SPACE_AROUND_UNARY_OPERATOR = false;
      commonSettings.SPACE_AFTER_COMMA = true;
      commonSettings.SPACE_AFTER_COMMA_IN_TYPE_ARGUMENTS = true;
      commonSettings.SPACE_BEFORE_COMMA = false;
      commonSettings.SPACE_AFTER_SEMICOLON = true; // in for-statement
      commonSettings.SPACE_BEFORE_SEMICOLON = false; // in for-statement
      commonSettings.SPACE_WITHIN_PARENTHESES = false;
      commonSettings.SPACE_WITHIN_METHOD_CALL_PARENTHESES = false;
      commonSettings.SPACE_WITHIN_METHOD_PARENTHESES = false;
      commonSettings.SPACE_WITHIN_IF_PARENTHESES = false;
      commonSettings.SPACE_WITHIN_WHILE_PARENTHESES = false;
      commonSettings.SPACE_WITHIN_FOR_PARENTHESES = false;
      commonSettings.SPACE_WITHIN_TRY_PARENTHESES = false;
      commonSettings.SPACE_WITHIN_CATCH_PARENTHESES = false;
      commonSettings.SPACE_WITHIN_SWITCH_PARENTHESES = false;
      commonSettings.SPACE_WITHIN_CAST_PARENTHESES = false;
      commonSettings.SPACE_WITHIN_BRACKETS = false;
      commonSettings.SPACE_WITHIN_BRACES = false;
      commonSettings.SPACE_WITHIN_ARRAY_INITIALIZER_BRACES = false;
      commonSettings.SPACE_AFTER_TYPE_CAST = false;
      commonSettings.SPACE_BEFORE_METHOD_CALL_PARENTHESES = false;
      commonSettings.SPACE_BEFORE_METHOD_PARENTHESES = false;
      commonSettings.SPACE_BEFORE_IF_PARENTHESES = true;
      commonSettings.SPACE_BEFORE_WHILE_PARENTHESES = true;
      commonSettings.SPACE_BEFORE_FOR_PARENTHESES = true;
      commonSettings.SPACE_BEFORE_TRY_PARENTHESES = true;
      commonSettings.SPACE_BEFORE_CATCH_PARENTHESES = true;
      commonSettings.SPACE_BEFORE_SWITCH_PARENTHESES = true;
      commonSettings.SPACE_BEFORE_CLASS_LBRACE = true;
      commonSettings.SPACE_BEFORE_METHOD_LBRACE = true;
      commonSettings.SPACE_BEFORE_IF_LBRACE = true;
      commonSettings.SPACE_BEFORE_ELSE_LBRACE = true;
      commonSettings.SPACE_BEFORE_WHILE_LBRACE = true;
      commonSettings.SPACE_BEFORE_FOR_LBRACE = true;
      commonSettings.SPACE_BEFORE_DO_LBRACE = true;
      commonSettings.SPACE_BEFORE_SWITCH_LBRACE = true;
      commonSettings.SPACE_BEFORE_TRY_LBRACE = true;
      commonSettings.SPACE_BEFORE_CATCH_LBRACE = true;
      commonSettings.SPACE_BEFORE_FINALLY_LBRACE = true;
      commonSettings.SPACE_BEFORE_ARRAY_INITIALIZER_LBRACE = false;

      commonSettings.SPACE_BEFORE_ELSE_KEYWORD = true;
      commonSettings.SPACE_BEFORE_WHILE_KEYWORD = true;
      commonSettings.SPACE_BEFORE_CATCH_KEYWORD = true;
      commonSettings.SPACE_BEFORE_FINALLY_KEYWORD = true;

      commonSettings.SPACE_BEFORE_QUEST = true;
      commonSettings.SPACE_AFTER_QUEST = true;
      commonSettings.SPACE_BEFORE_COLON = true;
      commonSettings.SPACE_AFTER_COLON = true;
      commonSettings.SPACE_BEFORE_TYPE_PARAMETER_LIST = false;

      commonSettings.CALL_PARAMETERS_WRAP = CodeStyleSettings.WRAP_AS_NEEDED;
      commonSettings.PREFER_PARAMETERS_WRAP = true;
      commonSettings.CALL_PARAMETERS_LPAREN_ON_NEXT_LINE = true;
      commonSettings.CALL_PARAMETERS_RPAREN_ON_NEXT_LINE = true;

      commonSettings.METHOD_PARAMETERS_WRAP = CodeStyleSettings.WRAP_AS_NEEDED;
      commonSettings.METHOD_PARAMETERS_LPAREN_ON_NEXT_LINE = true;
      commonSettings.METHOD_PARAMETERS_RPAREN_ON_NEXT_LINE = true;


      commonSettings.EXTENDS_KEYWORD_WRAP = CodeStyleSettings.DO_NOT_WRAP;
      commonSettings.THROWS_KEYWORD_WRAP = CodeStyleSettings.DO_NOT_WRAP;

      commonSettings.PARENTHESES_EXPRESSION_LPAREN_WRAP = false;
      commonSettings.PARENTHESES_EXPRESSION_RPAREN_WRAP = false;

      commonSettings.BINARY_OPERATION_WRAP = CodeStyleSettings.WRAP_AS_NEEDED;
      commonSettings.BINARY_OPERATION_SIGN_ON_NEXT_LINE = true;

      commonSettings.TERNARY_OPERATION_WRAP = CodeStyleSettings.WRAP_AS_NEEDED;
      commonSettings.TERNARY_OPERATION_SIGNS_ON_NEXT_LINE = true;

      commonSettings.MODIFIER_LIST_WRAP = false;

      commonSettings.FOR_STATEMENT_WRAP = CodeStyleSettings.WRAP_AS_NEEDED;
      commonSettings.FOR_STATEMENT_LPAREN_ON_NEXT_LINE = true;
      commonSettings.FOR_STATEMENT_RPAREN_ON_NEXT_LINE = true;

      commonSettings.ASSIGNMENT_WRAP = CodeStyleSettings.WRAP_AS_NEEDED;
      commonSettings.PLACE_ASSIGNMENT_SIGN_ON_NEXT_LINE = true;
      commonSettings.WRAP_COMMENTS = true;
      commonSettings.WRAP_LONG_LINES = false;
    }
  }

  public static class ZendCodeStyle extends PearCodeStyle {

    public ZendCodeStyle() {
      super("Zend");
    }

    @Override
    public void apply(CodeStyleSettings settings) {
      super.apply(settings);
      CommonCodeStyleSettings commonSettings = settings.getCommonSettings(getLanguage());
      commonSettings.ALIGN_MULTILINE_ARRAY_INITIALIZER_EXPRESSION = true;
      commonSettings.ALIGN_MULTILINE_EXTENDS_LIST = true;
      commonSettings.INDENT_CASE_FROM_SWITCH = true;

      CfmlCodeStyleSettings cfmlSettings = settings.getCustomSettings(CfmlCodeStyleSettings.class);
      cfmlSettings.ALIGN_KEY_VALUE_PAIRS = true;
    }
  }

  private static final String SPACING_CODE_SAMPLE = "<cffunction name=\"test\">\n" +
                                                    "\t<cfargument name=\"fred\" test=\"test\"/>\n" +
                                                    "\t<cfscript>\n" +
                                                    "\t\tWriteOutput(\"FREDFREDFRED\");\n" +
                                                    "function foo(x,y,z) {\n" +
                                                    "    bar(1,b);\n" +
                                                    " if (Fm >= Fl){Fm=Fl;}\n" +
                                                    " while (TC != Bl){Bo+=1;x++;}\n" +
                                                    " if (Bo == 1){\n" +
                                                    "  x=3*x-5 ;\n" +
                                                    " }else{\n" +
                                                    "     x=10;\n" +
                                                    " }\n" +
                                                    "x=0;\n" +
                                                    " for (x1=0; x1<10; x1++) {\n" +
                                                    "if (EX[x1] >= -50){\n" +
                                                    "  x=1;\n" +
                                                    " x = x1 && x; \n" +
                                                    " }\n" +
                                                    "}\n" +
                                                    "switch(x) {\n" +
                                                    " case 4:\n" +
                                                    "WriteOutput(\"q\");\n" +
                                                    " break; \n " +
                                                    "case 2:  \n" +
                                                    "   WriteOutput(\"a\");  \n" +
                                                    "  break;\n " +
                                                    "default: \n " +
                                                    "   WriteOutput(\"c\"); \n" +
                                                    "} \n" +
                                                    " try\n" +
                                                    "{ \n" +
                                                    "somethingWrong= x== 2 ? true : false ;\n" +
                                                    "c = b&d;\n" +
                                                    "throw(\"ExampleErrorType\",\"Example Error message.\");\n" +
                                                    "}" +
                                                    "catch (Any e)" +
                                                    "{ \n" +
                                                    "}\n" +
                                                    "do {Bo+=1;x++;\n}" +
                                                    " while (TC != Bl);\n" +
                                                    "\t</cfscript>\n" +
                                                    "\t<cfif thisisatest is 1>\n" +
                                                    "\t\t<cfoutput>asdfasdf</cfoutput>\n" +
                                                    "\t</cfif>\n" +
                                                    "</cffunction>\n" +
                                                    "<cfscript>\n" +
                                                    "\tif(find(\"some text\", agent ) and not find(\"some other\", agent ))\n" +
                                                    "\t{\n" +
                                                    "\t\t// comment string\n" +
                                                    "\t}\n" +
                                                    "</cfscript>";
  private static final String GENERAL_CODE_SAMPLE = SPACING_CODE_SAMPLE;
  private static final String BLANK_LINE_CODE_SAMPLE = "<cffunction name=\"test\">\n" +
                                                       "\t<cfargument name=\"fred\" test=\"test\"/>\n" +
                                                       "\n\n" +
                                                       "</cffunction>\n" +
                                                       "<cfoutput>\n" +
                                                       "\tThis is a test\n" +
                                                       "</cfoutput>\n";

  private static final String WRAPPING_CODE_SAMPLE = "<cffunction name=\"test\">\n" +
                                                     "\t<cfargument name=\"fred\" test=\"test\"/>\n" +
                                                     "\t<cfscript>\n" +
                                                     "\t\tWriteOutput(\"FREDFREDFRED\");\n" +
                                                     "function foo(x,y,z) {\n" +
                                                     "    bar(1,b);\n" +
                                                     " if (Fm >= Fl){Fm=Fl;}\n" +
                                                     "do {Bo+=1;x++;\n}" +
                                                     " while (TC != Bl);\n" +
                                                     " if (Bo == 1){\n" +
                                                     "  x=3*x-5 ;\n" +
                                                     " }else{\n" +
                                                     "     x=10;\n" +
                                                     " }\n" +
                                                     "x=0;\n" +
                                                     " for (x1=0; x1<10; x1++) {\n" +
                                                     "if (EX[x1] >= -50){\n" +
                                                     "  x=1;\n" +
                                                     " x = x1 && x; \n" +
                                                     " }\n" +
                                                     "}\n" +
                                                     " try\n" +
                                                     "{\n " +
                                                     "somethingWrong= x== 2 ? true : false ;\n" +
                                                     "c = b&d;\n" +
                                                     "throw(\"ExampleErrorType\",\"Example Error message.\");\n" +
                                                     "}\n" +
                                                     "catch (Any e)" +
                                                     "{ \n" +
                                                     "}\n" +
                                                     "\t</cfscript>\n" +
                                                     "\t<cfif thisisatest is 1>\n" +
                                                     "\t\t<cfoutput>asdfasdf</cfoutput>\n" +
                                                     "\t</cfif>\n" +
                                                     "</cffunction>\n" +
                                                     "<cfscript>\n" +
                                                     "\tif(find(\"some text\", agent ) and not find(\"some other\", agent ))\n" +
                                                     "\t{\n" +
                                                     "\t\t// comment string\n" +
                                                     "\t}\n" +
                                                     "</cfscript>";
}
