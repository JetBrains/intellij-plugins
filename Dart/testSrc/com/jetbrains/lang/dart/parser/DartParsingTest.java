package com.jetbrains.lang.dart.parser;

import com.intellij.testFramework.ParsingTestCase;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.DartParserDefinition;
import com.jetbrains.lang.dart.util.DartTestUtils;

public class DartParsingTest extends ParsingTestCase {
  public DartParsingTest() {
    super("parsing", DartFileType.DEFAULT_EXTENSION, new DartParserDefinition());
  }

  @Override
  protected String getTestDataPath() {
    return DartTestUtils.BASE_TEST_DATA_PATH;
  }

  @Override
  protected boolean skipSpaces() {
    return true;
  }

  private void doTest() {
    doTest(true);
  }

  public void testCascades() {
    doTest();
  }

  public void testClass1() {
    doTest();
  }

  public void testClass2() {
    doTest();
  }

  public void testConstructors() {
    doTest();
  }

  public void testEnum() {
    doTest();
  }

  public void testErrors() {
    doTest();
  }

  public void testFormalParameterSyntax() {
    doTest();
  }

  public void testFunctionTypeSyntax() {
    doTest();
  }

  public void testGeneric() {
    doTest();
  }

  public void testLiterals() {
    doTest();
  }

  public void testMethodSignatureSyntax() {
    doTest();
  }

  public void testMilestone1() {
    doTest();
  }

  public void testMilestone2() {
    doTest();
  }

  public void testMilestone3() {
    doTest();
  }

  public void testNullAwareInvalid() {
    doTest();
  }

  public void testNullAwareOperators() {
    doTest();
  }

  public void testOperators() {
    doTest();
  }

  public void testOther() {
    doTest();
  }

  public void testParametersAndArguments() {
    doTest();
  }

  public void testStrings() {
    doTest();
  }

  public void testSuperCallSyntax() {
    doTest();
  }

  public void testTopLevel() {
    doTest();
  }

  public void testTypedef() {
    doTest();
  }

  public void testLibrary() {
    doTest();
  }

  public void testHardCases1() {
    doTest();
  }

  public void testHardCases2() {
    doTest();
  }

  public void testHardCases3() {
    doTest();
  }

  public void testNotClosedComment1() {
    doTest();
  }

  public void testNotClosedComment2() {
    doTest();
  }

  public void testNotClosedComment3() {
    doTest();
  }

  public void testNotClosedComment4() {
    doTest();
  }

  public void testThrowExpression() {
    doTest();
  }

  public void testFunctionType() {
    doTest();
  }

  public void testAsyncForgotten() {
    doTest();
  }
}
