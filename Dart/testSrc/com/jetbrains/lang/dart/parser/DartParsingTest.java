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

  public void testCascades() throws Throwable {
    doTest();
  }

  public void testClass1() throws Throwable {
    doTest();
  }

  public void testClass2() throws Throwable {
    doTest();
  }

  public void testConstructors() throws Throwable {
    doTest();
  }

  public void testEnum() throws Throwable {
    doTest();
  }

  public void testErrors() throws Throwable {
    doTest();
  }

  public void testFormalParameterSyntax() throws Throwable {
    doTest();
  }

  public void testFunctionTypeSyntax() throws Throwable {
    doTest();
  }

  public void testGeneric() throws Throwable {
    doTest();
  }

  public void testLiterals() throws Throwable {
    doTest();
  }

  public void testMethodSignatureSyntax() throws Throwable {
    doTest();
  }

  public void testMilestone1() throws Throwable {
    doTest();
  }

  public void testMilestone2() throws Throwable {
    doTest();
  }

  public void testMilestone3() throws Throwable {
    doTest();
  }

  public void testNullAwareInvalid() throws Throwable {
    doTest();
  }

  public void testNullAwareOperators() throws Throwable {
    doTest();
  }

  public void testOperators() throws Throwable {
    doTest();
  }

  public void testOther() throws Throwable {
    doTest();
  }

  public void testParametersAndArguments() throws Throwable {
    doTest();
  }

  public void testStrings() throws Throwable {
    doTest();
  }

  public void testSuperCallSyntax() throws Throwable {
    doTest();
  }

  public void testTopLevel() throws Throwable {
    doTest();
  }

  public void testTypedef() throws Throwable {
    doTest();
  }

  public void testLibrary() throws Throwable {
    doTest();
  }

  public void testHardCases1() throws Throwable {
    doTest();
  }

  public void testHardCases2() throws Throwable {
    doTest();
  }

  public void testHardCases3() throws Throwable {
    doTest();
  }

  public void testNotClosedComment1() throws Throwable {
    doTest();
  }

  public void testNotClosedComment2() throws Throwable {
    doTest();
  }

  public void testNotClosedComment3() throws Throwable {
    doTest();
  }

  public void testNotClosedComment4() throws Throwable {
    doTest();
  }

  public void testThrowExpression() throws Throwable {
    doTest();
  }

  public void testFunctionType() throws Throwable {
    doTest();
  }
}
