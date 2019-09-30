// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.formatter;

import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.formatter.FormatterTestCase;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.util.DartTestUtils;

public class DartFormatterTest extends FormatterTestCase {

  @Override
  protected String getFileExtension() {
    return DartFileType.DEFAULT_EXTENSION;
  }

  @Override
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
    doTest();
  }

  public void testBracePlacement1() throws Exception {
    doTest();
  }

  public void testBracePlacement2() throws Exception {
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
    doTest();
  }

  public void testSpaceAroundOperators() throws Exception {
    doTest();
  }

  public void testSpaceBeforeParentheses() throws Exception {
    doTest();
  }

  public void testSpaceLeftBraces() throws Exception {
    doTest();
  }

  public void testSpaceOthers() throws Exception {
    doTest();
  }

  public void testSpaceWithin() throws Exception {
    doTest();
  }

  public void testWrappingMeth() throws Exception {
    doTest();
  }

  public void testComments() throws Exception {
    doTest();
  }

  public void testLineCommentsAtFirstColumn() throws Exception {
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
    doTest();
  }

  public void testOddCases() throws Exception {
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
