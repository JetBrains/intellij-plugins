// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.model.formatter;


import com.intellij.application.options.IndentOptionsEditor;
import com.intellij.application.options.SmartIndentOptionsEditor;
import com.intellij.coldFusion.CfmlBundle;
import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.psi.codeStyle.CodeStyleSettingsCustomizableOptions.getInstance;

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
      consumer.showCustomOption(CfmlCodeStyleSettings.class, "CONCAT_SPACES", CfmlBundle.message("settings.code.style.concatenation"),
                                getInstance().SPACES_AROUND_OPERATORS);
    }
    else if (settingsType == SettingsType.BLANK_LINES_SETTINGS) {
      consumer.showStandardOptions("KEEP_BLANK_LINES_IN_CODE");
    }
    else if (settingsType == SettingsType.WRAPPING_AND_BRACES_SETTINGS) {
      consumer.showStandardOptions("RIGHT_MARGIN",
                                   "KEEP_LINE_BREAKS",
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
  protected void customizeDefaults(@NotNull CommonCodeStyleSettings commonSettings,
                                   @NotNull CommonCodeStyleSettings.IndentOptions indentOptions) {
    commonSettings.CLASS_BRACE_STYLE = CommonCodeStyleSettings.NEXT_LINE;
    commonSettings.METHOD_BRACE_STYLE = CommonCodeStyleSettings.NEXT_LINE;
    commonSettings.SPECIAL_ELSE_IF_TREATMENT = false;
    commonSettings.SPACE_AFTER_TYPE_CAST = false;
  }

  @Nullable
  @Override
  public IndentOptionsEditor getIndentOptionsEditor() {
    return new SmartIndentOptionsEditor();
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
