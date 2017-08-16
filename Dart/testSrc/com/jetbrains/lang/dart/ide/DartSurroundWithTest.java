package com.jetbrains.lang.dart.ide;

import com.intellij.codeInsight.generation.surroundWith.SurroundWithHandler;
import com.intellij.lang.surroundWith.Surrounder;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.LightPlatformCodeInsightTestCase;
import com.jetbrains.lang.dart.ide.surroundWith.expression.DartWithBracketsExpressionSurrounder;
import com.jetbrains.lang.dart.ide.surroundWith.expression.DartWithNotParenthesisExpressionSurrounder;
import com.jetbrains.lang.dart.ide.surroundWith.expression.DartWithParenthesisExpressionSurrounder;
import com.jetbrains.lang.dart.ide.surroundWith.statement.*;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.jetbrains.annotations.NotNull;

public class DartSurroundWithTest extends LightPlatformCodeInsightTestCase {
  @NotNull
  @Override
  protected String getTestDataPath() {
    return DartTestUtils.BASE_TEST_DATA_PATH + FileUtil.toSystemDependentName("/surroundWith/");
  }

  private void doTest(final Surrounder handler) {
    configureByFile(getTestName(false) + ".dart");
    SurroundWithHandler.invoke(getProject(), getEditor(), getFile(), handler);

    checkResultByFile(getTestName(false) + ".after.dart");
  }

  public void testDoWhile1() {
    doTest(new DartWithDoWhileSurrounder());
  }

  public void testDoWhile2() {
    doTest(new DartWithDoWhileSurrounder());
  }


  public void testFor1() {
    doTest(new DartWithForSurrounder());
  }

  public void testFor2() {
    doTest(new DartWithForSurrounder());
  }

  public void testNotParenthesis1() {
    doTest(new DartWithNotParenthesisExpressionSurrounder());
  }

  public void testNotParenthesis2() {
    doTest(new DartWithNotParenthesisExpressionSurrounder());
  }

  public void testIf1() {
    doTest(new DartWithIfSurrounder());
  }

  public void testIf2() {
    doTest(new DartWithIfSurrounder());
  }

  public void testIfElse1() {
    doTest(new DartWithIfElseSurrounder());
  }

  public void testIfElse2() {
    doTest(new DartWithIfElseSurrounder());
  }

  public void testParenthesis1() {
    doTest(new DartWithParenthesisExpressionSurrounder());
  }

  public void testParenthesis2() {
    doTest(new DartWithParenthesisExpressionSurrounder());
  }

  public void testTryCatch() {
    doTest(new DartWithTryCatchSurrounder());
  }

  public void testTryCatchFinally() {
    doTest(new DartWithTryCatchFinallySurrounder());
  }

  public void testWhile1() {
    doTest(new DartWithWhileSurrounder());
  }

  public void testWhile2() {
    doTest(new DartWithWhileSurrounder());
  }

  public void testBrackets1() {
    doTest(new DartWithBracketsExpressionSurrounder());
  }

  public void testBrackets2() {
    doTest(new DartWithBracketsExpressionSurrounder());
  }
}
