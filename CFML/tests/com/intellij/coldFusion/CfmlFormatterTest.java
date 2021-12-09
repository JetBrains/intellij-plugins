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
package com.intellij.coldFusion;

import com.intellij.application.options.CodeStyle;
import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.coldFusion.model.files.CfmlFileType;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import junit.framework.Assert;

public class CfmlFormatterTest extends CfmlCodeInsightFixtureTestCase {
  protected CommonCodeStyleSettings myTestStyleSettings;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    setTestStyleSettings();
  }

  public void testIndent() {
    doTest();
  }

  public void testActionIndention() {
    doTest();
  }

  public void testSytheticBlockIndention() {
    doTest();
  }

  public void testSpaceBeforeParentheses() {
    myTestStyleSettings.SPACE_BEFORE_METHOD_CALL_PARENTHESES = true;
    myTestStyleSettings.SPACE_BEFORE_METHOD_PARENTHESES = true;
    myTestStyleSettings.SPACE_BEFORE_IF_PARENTHESES = false;
    myTestStyleSettings.SPACE_BEFORE_FOR_PARENTHESES = false;
    myTestStyleSettings.SPACE_BEFORE_WHILE_PARENTHESES = false;
    myTestStyleSettings.SPACE_BEFORE_SWITCH_PARENTHESES = false;
    myTestStyleSettings.SPACE_BEFORE_CATCH_PARENTHESES = false;
    doTest();
  }

  public void testSpaceAroundOperators() {
    myTestStyleSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS = false;
    myTestStyleSettings.SPACE_AROUND_LOGICAL_OPERATORS = false;
    myTestStyleSettings.SPACE_AROUND_EQUALITY_OPERATORS = false;
    myTestStyleSettings.SPACE_AROUND_RELATIONAL_OPERATORS = false;
    myTestStyleSettings.SPACE_AROUND_ADDITIVE_OPERATORS = false;
    myTestStyleSettings.SPACE_AROUND_MULTIPLICATIVE_OPERATORS = false;
    doTest();
  }

  public void testSpaceLeftBraces() {
    myTestStyleSettings.SPACE_BEFORE_METHOD_LBRACE = false;
    myTestStyleSettings.SPACE_BEFORE_IF_LBRACE = false;
    myTestStyleSettings.SPACE_BEFORE_ELSE_LBRACE = false;
    myTestStyleSettings.SPACE_BEFORE_FOR_LBRACE = false;
    myTestStyleSettings.SPACE_BEFORE_WHILE_LBRACE = false;
    myTestStyleSettings.SPACE_BEFORE_SWITCH_LBRACE = false;
    myTestStyleSettings.SPACE_BEFORE_TRY_LBRACE = false;
    myTestStyleSettings.SPACE_BEFORE_CATCH_LBRACE = false;
    doTest();
  }

  public void testSpaceWithin() {
    myTestStyleSettings.SPACE_WITHIN_METHOD_CALL_PARENTHESES = true;
    myTestStyleSettings.SPACE_WITHIN_METHOD_PARENTHESES = true;
    myTestStyleSettings.SPACE_WITHIN_IF_PARENTHESES = true;
    myTestStyleSettings.SPACE_WITHIN_FOR_PARENTHESES = true;
    myTestStyleSettings.SPACE_WITHIN_WHILE_PARENTHESES = true;
    myTestStyleSettings.SPACE_WITHIN_SWITCH_PARENTHESES = true;
    myTestStyleSettings.SPACE_WITHIN_CATCH_PARENTHESES = true;
    doTest();
  }

  public void testSpaceOthers() {
    myTestStyleSettings.SPACE_BEFORE_WHILE_KEYWORD = false;
    myTestStyleSettings.SPACE_BEFORE_CATCH_KEYWORD = false;
    myTestStyleSettings.SPACE_BEFORE_ELSE_KEYWORD = false;
    myTestStyleSettings.SPACE_BEFORE_QUEST = false;
    myTestStyleSettings.SPACE_AFTER_QUEST = false;
    myTestStyleSettings.SPACE_BEFORE_COLON = false;
    myTestStyleSettings.SPACE_AFTER_COLON = false;
    myTestStyleSettings.SPACE_BEFORE_COMMA = true;
    myTestStyleSettings.SPACE_AFTER_COMMA = false;
    myTestStyleSettings.SPACE_BEFORE_SEMICOLON = true;
    myTestStyleSettings.SPACE_AFTER_SEMICOLON = false;
    doTest();
  }

  public void testWrappingMeth() {
    myTestStyleSettings.METHOD_ANNOTATION_WRAP = CommonCodeStyleSettings.WRAP_AS_NEEDED;
    myTestStyleSettings.METHOD_PARAMETERS_LPAREN_ON_NEXT_LINE = true;
    myTestStyleSettings.METHOD_PARAMETERS_RPAREN_ON_NEXT_LINE = true;
    myTestStyleSettings.CALL_PARAMETERS_WRAP = CommonCodeStyleSettings.WRAP_AS_NEEDED;
    myTestStyleSettings.CALL_PARAMETERS_LPAREN_ON_NEXT_LINE = true;
    myTestStyleSettings.CALL_PARAMETERS_RPAREN_ON_NEXT_LINE = true;
    myTestStyleSettings.ELSE_ON_NEW_LINE = true;
    myTestStyleSettings.SPECIAL_ELSE_IF_TREATMENT = true;
    myTestStyleSettings.FOR_STATEMENT_WRAP = CommonCodeStyleSettings.WRAP_AS_NEEDED;
    myTestStyleSettings.FOR_STATEMENT_LPAREN_ON_NEXT_LINE = true;
    myTestStyleSettings.FOR_STATEMENT_RPAREN_ON_NEXT_LINE = true;
    myTestStyleSettings.WHILE_ON_NEW_LINE = true;
    myTestStyleSettings.CATCH_ON_NEW_LINE = true;
    myTestStyleSettings.BINARY_OPERATION_WRAP = CommonCodeStyleSettings.WRAP_AS_NEEDED;
    myTestStyleSettings.BINARY_OPERATION_SIGN_ON_NEXT_LINE = true;
    myTestStyleSettings.PARENTHESES_EXPRESSION_LPAREN_WRAP = true;
    myTestStyleSettings.PARENTHESES_EXPRESSION_RPAREN_WRAP = true;
    myTestStyleSettings.ASSIGNMENT_WRAP = CommonCodeStyleSettings.WRAP_AS_NEEDED;
    myTestStyleSettings.PLACE_ASSIGNMENT_SIGN_ON_NEXT_LINE = true;
    myTestStyleSettings.TERNARY_OPERATION_WRAP = CommonCodeStyleSettings.WRAP_AS_NEEDED;
    myTestStyleSettings.TERNARY_OPERATION_SIGNS_ON_NEXT_LINE = true;
    myTestStyleSettings.BLOCK_COMMENT_AT_FIRST_COLUMN = true;
    doTest();
  }

  public void testAlignment() {
    myTestStyleSettings.ALIGN_MULTILINE_PARAMETERS = true;
    myTestStyleSettings.ALIGN_MULTILINE_FOR = false;
    myTestStyleSettings.ALIGN_MULTILINE_TERNARY_OPERATION = true;
    myTestStyleSettings.ALIGN_MULTILINE_BINARY_OPERATION = true;
    doTest();
  }

  public void testAlignment2() {
    doTest();
  }

  public void testAlignment3() throws Exception {
    Util.runTestWithLanguageLevel(() -> {
      doTest();
      return null;
    }, CfmlLanguage.RAILO, getProject());
  }

  public void testBracePlacement1() {
    myTestStyleSettings.BRACE_STYLE = CommonCodeStyleSettings.NEXT_LINE_SHIFTED2;
    myTestStyleSettings.METHOD_BRACE_STYLE = CommonCodeStyleSettings.NEXT_LINE;
    doTest();
  }

  public void testBracePlacement2() {
    myTestStyleSettings.BRACE_STYLE = CommonCodeStyleSettings.END_OF_LINE;
    myTestStyleSettings.METHOD_BRACE_STYLE = CommonCodeStyleSettings.NEXT_LINE_SHIFTED;
    doTest();
  }

  public void testSpacingInCfSetTagIfWrap() {
    myTestStyleSettings.ASSIGNMENT_WRAP = CommonCodeStyleSettings.WRAP_AS_NEEDED;
    myTestStyleSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS = false;
    doTest();
  }

  public void testElseIfSpecialStatementIndention() {
    myTestStyleSettings.SPECIAL_ELSE_IF_TREATMENT = true;
    myTestStyleSettings.SPACE_AROUND_ADDITIVE_OPERATORS = false;
    doTest();
  }
  
  
  public void testIndentOptions() {
    CommonCodeStyleSettings.IndentOptions indentOptions = myTestStyleSettings.getIndentOptions();
    assertNotNull(indentOptions);
    indentOptions.INDENT_SIZE = 2;
    doTest();
  }


  private void setTestStyleSettings() {
    Project project = getProject();
    CodeStyleSettings currSettings = CodeStyle.getSettings(project);
    Assert.assertNotNull(currSettings);
    CodeStyleSettings tempSettings = CodeStyle.createTestSettings(currSettings);
    CommonCodeStyleSettings.IndentOptions indentOptions = tempSettings.getIndentOptions(CfmlFileType.INSTANCE);
    Assert.assertNotNull(indentOptions);
    defineStyleSettings(tempSettings);
    CodeStyleSettingsManager.getInstance(project).setTemporarySettings(tempSettings);
  }


  protected void defineStyleSettings(CodeStyleSettings tempSettings) {
    myTestStyleSettings = tempSettings.getCommonSettings(CfmlLanguage.INSTANCE);
    myTestStyleSettings.KEEP_BLANK_LINES_IN_CODE = 2;
    myTestStyleSettings.METHOD_BRACE_STYLE = CommonCodeStyleSettings.END_OF_LINE;
    myTestStyleSettings.BRACE_STYLE = CommonCodeStyleSettings.END_OF_LINE;
    myTestStyleSettings.ALIGN_MULTILINE_PARAMETERS = false;
    myTestStyleSettings.ALIGN_MULTILINE_PARAMETERS_IN_CALLS = false;
    myTestStyleSettings.KEEP_FIRST_COLUMN_COMMENT = true;
  }


  private void doTest() {
    myFixture.configureByFile(Util.getInputDataFileName(getTestName(true)));
    WriteCommandAction.runWriteCommandAction(null, () -> {
      CodeStyleManager.getInstance(myFixture.getProject()).reformat(myFixture.getFile());
    });
    myFixture.checkResultByFile(Util.getExpectedDataFileName(getTestName(true)));
  }

  @Override
  protected String getBasePath() {
    return "/formatter";
  }
}
