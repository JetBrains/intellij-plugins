package com.jetbrains.lang.dart.rename;

import com.intellij.openapi.util.io.FileUtil;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;

/**
 * @author: Fedor.Korotkov
 */
public class DartRenameTest extends DartCodeInsightFixtureTestCase {
  @Override
  protected String getBasePath() {
    return FileUtil.toSystemDependentName("/rename/");
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

  public void testConstructor3() throws Throwable {
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

  public void testLibrary2() throws Throwable {
    doTest("otherLib.bar", "additional/myQLibPart.dart");
    myFixture.checkResultByFile("additional/myQLibPart.dart", "additional/myQLibPartAfter.dart", true);
  }

  public void testMethod() throws Throwable {
    doTest("fooNew");
  }

  public void testOverriddenMethod1() throws Throwable {
    doTest("fooNew");
  }

  public void testOverriddenMethod2() throws Throwable {
    doTest("fooNew");
  }

  public void testOverriddenMethod3() throws Throwable {
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

  public void testWEB_6708() throws Throwable {
    doTest("renamed");
  }

  public void testWEB_7218() throws Throwable {
    doTest("colorTextNew");
  }
}
