// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.jetbrains.lang.dart.formatter;

import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.formatter.FormatterTestCase;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.util.DartTestUtils;

public class DartFormatterTest extends FormatterTestCase {

  protected String getFileExtension() {
    return DartFileType.DEFAULT_EXTENSION;
  }

  protected String getTestDataPath() {
    return DartTestUtils.BASE_TEST_DATA_PATH;
  }

  @Override
  protected String getBasePath() {
    return "formatter";
  }

  @Override
  protected void doTest(String resultNumber) throws Exception {
    String testName = getTestName(false);
    doTest(testName + "." + getFileExtension(), testName + "_after." + getFileExtension(), resultNumber);
  }

  public void testAlignment() throws Exception {
    final CommonCodeStyleSettings settings = getSettings(DartLanguage.INSTANCE);
    settings.ALIGN_MULTILINE_PARAMETERS = true;
    settings.ALIGN_MULTILINE_BINARY_OPERATION = true;
    settings.ALIGN_MULTILINE_TERNARY_OPERATION = true;
    settings.KEEP_LINE_BREAKS = true;
    doTest();
  }

  public void testBracePlacement1() throws Exception {
    final CommonCodeStyleSettings settings = getSettings(DartLanguage.INSTANCE);
    settings.KEEP_LINE_BREAKS = false;
    settings.BRACE_STYLE = CommonCodeStyleSettings.NEXT_LINE_SHIFTED2;
    settings.METHOD_BRACE_STYLE = CommonCodeStyleSettings.NEXT_LINE;
    doTest();
  }

  public void testBracePlacement2() throws Exception {
    final CommonCodeStyleSettings settings = getSettings(DartLanguage.INSTANCE);
    settings.KEEP_LINE_BREAKS = false;
    settings.BRACE_STYLE = CommonCodeStyleSettings.END_OF_LINE;
    settings.METHOD_BRACE_STYLE = CommonCodeStyleSettings.NEXT_LINE_SHIFTED;
    doTest();
  }

  public void testDefault() throws Exception {
    doTest();
  }

  public void testDefault2() throws Exception {
    doTest();
  }

  public void testDefault3() throws Exception {
    doTest();
  }

  public void testDefaultAll() throws Exception {
    doTest();
  }

  public void testWEB_7058() throws Exception {
    doTest();
  }

  public void testCascades() throws Exception {
    doTest();
  }

  public void testCascadeSame() throws Exception {
    doTest();
  }

  public void testStrings() throws Exception {
    doTest();
  }

  public void testAdjacentStrings() throws Exception {
    doTest();
  }

  public void testAdjacentStringsLong() throws Exception {
    doTest();
  }

  public void testExpressions() throws Exception {
    final CommonCodeStyleSettings settings = getSettings(DartLanguage.INSTANCE);
    settings.RIGHT_MARGIN = 40;
    settings.KEEP_LINE_BREAKS = false; // TODO Decide whether this should be the default -- risky!
    doTest();
  }

  public void testSpaceAroundOperators() throws Exception {
    final CommonCodeStyleSettings settings = getSettings(DartLanguage.INSTANCE);
    settings.KEEP_LINE_BREAKS = false;
    settings.SPACE_AROUND_ASSIGNMENT_OPERATORS = false;
    settings.SPACE_AROUND_LOGICAL_OPERATORS = false;
    settings.SPACE_AROUND_EQUALITY_OPERATORS = false;
    settings.SPACE_AROUND_RELATIONAL_OPERATORS = false;
    settings.SPACE_AROUND_ADDITIVE_OPERATORS = false;
    settings.SPACE_AROUND_MULTIPLICATIVE_OPERATORS = false;
    doTest();
  }

  public void testSpaceBeforeParentheses() throws Exception {
    final CommonCodeStyleSettings settings = getSettings(DartLanguage.INSTANCE);
    settings.KEEP_LINE_BREAKS = false;
    settings.SPACE_BEFORE_METHOD_CALL_PARENTHESES = true;
    settings.SPACE_BEFORE_METHOD_PARENTHESES = true;
    settings.SPACE_BEFORE_IF_PARENTHESES = false;
    settings.SPACE_BEFORE_FOR_PARENTHESES = false;
    settings.SPACE_BEFORE_WHILE_PARENTHESES = false;
    settings.SPACE_BEFORE_SWITCH_PARENTHESES = false;
    settings.SPACE_BEFORE_CATCH_PARENTHESES = false;
    doTest();
  }

  public void testSpaceLeftBraces() throws Exception {
    final CommonCodeStyleSettings settings = getSettings(DartLanguage.INSTANCE);
    settings.KEEP_LINE_BREAKS = false;
    settings.SPACE_BEFORE_METHOD_LBRACE = false;
    settings.SPACE_BEFORE_IF_LBRACE = false;
    settings.SPACE_BEFORE_ELSE_LBRACE = false;
    settings.SPACE_BEFORE_FOR_LBRACE = false;
    settings.SPACE_BEFORE_WHILE_LBRACE = false;
    settings.SPACE_BEFORE_SWITCH_LBRACE = false;
    settings.SPACE_BEFORE_TRY_LBRACE = false;
    settings.SPACE_BEFORE_CATCH_LBRACE = false;
    doTest();
  }

  public void testSpaceOthers() throws Exception {
    final CommonCodeStyleSettings settings = getSettings(DartLanguage.INSTANCE);
    settings.KEEP_LINE_BREAKS = false;
    settings.SPACE_BEFORE_WHILE_KEYWORD = false;
    settings.SPACE_BEFORE_CATCH_KEYWORD = false;
    settings.SPACE_BEFORE_ELSE_KEYWORD = false;
    settings.SPACE_BEFORE_QUEST = false;
    settings.SPACE_AFTER_QUEST = false;
    settings.SPACE_BEFORE_COLON = false;
    settings.SPACE_AFTER_COLON = false;
    settings.SPACE_BEFORE_COMMA = true;
    settings.SPACE_AFTER_COMMA = false;
    settings.SPACE_BEFORE_SEMICOLON = true;
    settings.SPACE_AFTER_SEMICOLON = false;
    doTest();
  }

  public void testSpaceWithin() throws Exception {
    final CommonCodeStyleSettings settings = getSettings(DartLanguage.INSTANCE);
    settings.KEEP_LINE_BREAKS = false;
    settings.SPACE_WITHIN_METHOD_CALL_PARENTHESES = true;
    settings.SPACE_WITHIN_METHOD_PARENTHESES = true;
    settings.SPACE_WITHIN_IF_PARENTHESES = true;
    settings.SPACE_WITHIN_FOR_PARENTHESES = true;
    settings.SPACE_WITHIN_WHILE_PARENTHESES = true;
    settings.SPACE_WITHIN_SWITCH_PARENTHESES = true;
    settings.SPACE_WITHIN_CATCH_PARENTHESES = true;
    doTest();
  }

  public void testWrappingMeth() throws Exception {
    final CommonCodeStyleSettings settings = getSettings(DartLanguage.INSTANCE);
    settings.METHOD_ANNOTATION_WRAP = CommonCodeStyleSettings.WRAP_AS_NEEDED;
    settings.METHOD_PARAMETERS_LPAREN_ON_NEXT_LINE = true;
    settings.METHOD_PARAMETERS_RPAREN_ON_NEXT_LINE = true;
    settings.CALL_PARAMETERS_WRAP = CommonCodeStyleSettings.WRAP_AS_NEEDED;
    settings.CALL_PARAMETERS_LPAREN_ON_NEXT_LINE = true;
    settings.CALL_PARAMETERS_RPAREN_ON_NEXT_LINE = true;
    settings.ELSE_ON_NEW_LINE = true;
    settings.SPECIAL_ELSE_IF_TREATMENT = true;
    settings.FOR_STATEMENT_WRAP = CommonCodeStyleSettings.WRAP_AS_NEEDED;
    settings.FOR_STATEMENT_LPAREN_ON_NEXT_LINE = true;
    settings.FOR_STATEMENT_RPAREN_ON_NEXT_LINE = true;
    settings.WHILE_ON_NEW_LINE = true;
    settings.CATCH_ON_NEW_LINE = true;
    settings.BINARY_OPERATION_WRAP = CommonCodeStyleSettings.WRAP_AS_NEEDED;
    settings.BINARY_OPERATION_SIGN_ON_NEXT_LINE = true;
    settings.PARENTHESES_EXPRESSION_LPAREN_WRAP = true;
    settings.PARENTHESES_EXPRESSION_RPAREN_WRAP = true;
    settings.ASSIGNMENT_WRAP = CommonCodeStyleSettings.WRAP_AS_NEEDED;
    settings.PLACE_ASSIGNMENT_SIGN_ON_NEXT_LINE = true;
    settings.TERNARY_OPERATION_WRAP = CommonCodeStyleSettings.WRAP_AS_NEEDED;
    settings.TERNARY_OPERATION_SIGNS_ON_NEXT_LINE = true;
    settings.BLOCK_COMMENT_AT_FIRST_COLUMN = true;
    settings.KEEP_LINE_BREAKS = true;
    doTest();
  }

  public void testComments() throws Exception {
    getSettings(DartLanguage.INSTANCE).KEEP_FIRST_COLUMN_COMMENT = false;
    doTest();
  }

  public void testLineCommentsAtFirstColumn() throws Exception {
    getSettings(DartLanguage.INSTANCE).KEEP_FIRST_COLUMN_COMMENT = true;
    doTest();
  }

  public void testMetadata() throws Exception {
    doTest();
  }

  public void testMapAndListLiterals() throws Exception {
    doTest();
  }

  public void testEmptyBlocks() throws Exception {
    doTest();
  }

  public void testTernary1() throws Exception {
    final CommonCodeStyleSettings settings = getSettings(DartLanguage.INSTANCE);
    settings.RIGHT_MARGIN = 40;
    doTest();
  }

  public void testVarDecl() throws Exception {
    final CommonCodeStyleSettings settings = getSettings(DartLanguage.INSTANCE);
    settings.RIGHT_MARGIN = 40;
    doTest();
  }

  public void testFileComments() throws Exception {
    final CommonCodeStyleSettings settings = getSettings(DartLanguage.INSTANCE);
    settings.RIGHT_MARGIN = 40;
    doTest();
  }

  public void testOddCases() throws Exception {
    final CommonCodeStyleSettings settings = getSettings(DartLanguage.INSTANCE);
    settings.BRACE_STYLE = CommonCodeStyleSettings.NEXT_LINE_IF_WRAPPED;
    doTest();
  }

  public void testArguments() throws Exception {
    doTest();
  }

  public void testArgumentComment() throws Exception {
    doTest();
  }

  public void testAsyncForgotten() throws Exception {
    doTest();
  }

  public void testSyntaxErrors() throws Exception {
    doTest();
  }

  public void testErrorInArgumentList() throws Exception {
    doTest();
  }

  public void testConstructorsWithoutNew() throws Exception {
    doTest();
  }
}
