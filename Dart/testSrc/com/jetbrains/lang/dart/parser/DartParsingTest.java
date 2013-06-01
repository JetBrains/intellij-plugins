package com.jetbrains.lang.dart.parser;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.ParsingTestCase;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.DartParserDefinition;

/**
 * @author: Fedor.Korotkov
 */
public class DartParsingTest extends ParsingTestCase {
  public DartParsingTest() {
    super("parsing", DartFileType.DEFAULT_EXTENSION, new DartParserDefinition());
  }

  @Override
  protected String getTestDataPath() {
    return PathManager.getHomePath() + FileUtil.toSystemDependentName("/plugins/Dart/testData/");
  }

  @Override
  protected boolean skipSpaces() {
    return true;
  }

  public void testAbstractMethodSyntax() throws Throwable {
    doTest(true);
  }

  public void testCascades() throws Throwable {
    doTest(true);
  }

  public void testClass1() throws Throwable {
    doTest(true);
  }

  public void testClass2() throws Throwable {
    doTest(true);
  }

  public void testConstructors() throws Throwable {
    doTest(true);
  }

  public void testErrors() throws Throwable {
    doTest(true);
  }

  public void testFormalParameterSyntax() throws Throwable {
    doTest(true);
  }

  public void testFunctionTypeSyntax() throws Throwable {
    doTest(true);
  }

  public void testGeneric() throws Throwable {
    doTest(true);
  }

  public void testLiterals() throws Throwable {
    doTest(true);
  }

  public void testMethodSignatureSyntax() throws Throwable {
    doTest(true);
  }

  public void testMilestone1() throws Throwable {
    doTest(true);
  }

  public void testMilestone2() throws Throwable {
    doTest(true);
  }

  public void testMilestone3() throws Throwable {
    doTest(true);
  }

  public void testOperators() throws Throwable {
    doTest(true);
  }

  public void testOther() throws Throwable {
    doTest(true);
  }

  public void testParametersAndArguments() throws Throwable {
    doTest(true);
  }

  public void testSetGetSyntax() throws Throwable {
    doTest(true);
  }

  public void testStrings() throws Throwable {
    doTest(true);
  }

  public void testSuperCallSyntax() throws Throwable {
    doTest(true);
  }

  public void testTopLevel() throws Throwable {
    doTest(true);
  }

  public void testTypedef() throws Throwable {
    doTest(true);
  }

  public void testLibrary() throws Throwable {
    doTest(true);
  }

  public void testHardCases1() throws Throwable {
    doTest(true);
  }

  public void testHardCases2() throws Throwable {
    doTest(true);
  }

  public void testHardCases3() throws Throwable {
    doTest(true);
  }
}
