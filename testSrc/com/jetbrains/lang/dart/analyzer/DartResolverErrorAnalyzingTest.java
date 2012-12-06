package com.jetbrains.lang.dart.analyzer;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;

public class DartResolverErrorAnalyzingTest extends DartAnalyzerTestBase {
  @Override
  protected String getTestDataPath() {
    return PathManager.getHomePath() + FileUtil.toSystemDependentName("/web-ide/WebStorm/Dart/testData/analyzer/resolver");
  }

  public void testCannotResolveMethod1() throws Throwable {
    doTest("cannot resolve method 'foo'");
  }

  public void testCannotResolveMethod2() throws Throwable {
    doTest("cannot resolve method 'add'");
  }

  public void testCannotResolveMethod3() throws Throwable {
    doTest("cannot resolve method 'add'");
  }

  public void testCannotResolveMethodInClass1() throws Throwable {
    doTest("cannot resolve method 'bar' in class 'Foo'");
  }

  public void testFieldDoesNotHaveAGetter1() throws Throwable {
    doTest("Field does not have a getter");
  }

  public void testFieldDoesNotHaveASetter1() throws Throwable {
    doTest("Field does not have a setter");
  }

  public void testNotAStaticField() throws Throwable {
    doTest("\"bar\" is not a static field");
  }

  public void testNotAStaticMethod() throws Throwable {
    doTest("\"bar\" is not a static method");
  }
}
