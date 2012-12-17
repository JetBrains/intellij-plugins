package com.jetbrains.lang.dart.rename;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;

/**
 * @author: Fedor.Korotkov
 */
public class DartRenameTest extends JavaCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return PathManager.getHomePath() + FileUtil.toSystemDependentName("/plugins/Dart/testData/rename/");
  }

  public void doTest(String newName, String... additionalFiles) {
    myFixture.testRename(getTestName(false) + ".dart", getTestName(false) + "After.dart", newName, additionalFiles);
  }

  public void testConstructor1() throws Throwable {
    doTest("FooNew");
  }

  public void testConstructor2() throws Throwable {
    doTest("FooNew");
  }

  public void testLocalVariable() throws Throwable {
    doTest("fooNew");
  }

  public void testFunctionParameter() throws Throwable {
    doTest("fooNew");
  }

  public void testLibrary1() throws Throwable {
    doTest("otherLib", "additional/myLibPart.dart");
    myFixture.checkResultByFile("additional/myLibPart.dart", "additional/myLibPartAfter.dart", true);
  }

  public void testMethod() throws Throwable {
    doTest("fooNew");
  }

  public void testMainClass() throws Throwable {
    doTest("MainClassAfter");
  }

  public void testStaticField() throws Throwable {
    doTest("fooNew", "additional/StaticFieldHelper.dart");
  }

  public void testStaticMethod() throws Throwable {
    doTest("fooNew", "additional/StaticMethodHelper.dart");
  }

  public void testStrings() throws Throwable {
    doTest("hello");
  }
}
