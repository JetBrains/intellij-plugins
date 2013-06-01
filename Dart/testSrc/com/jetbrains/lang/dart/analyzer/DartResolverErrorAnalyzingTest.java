package com.jetbrains.lang.dart.analyzer;

import com.intellij.openapi.util.io.FileUtil;

public class DartResolverErrorAnalyzingTest extends DartAnalyzerTestBase {
  @Override
  protected String getBasePath() {
    return FileUtil.toSystemDependentName("/plugins/Dart/testData/analyzer/resolver");
  }

  public void testCannotResolveMethod1() throws Throwable {
    doTest("The FUNCTION 'foo' is not defined");
  }

  public void testCannotResolveMethod2() throws Throwable {
    doTest("The FUNCTION 'add' is not defined");
  }

  public void testCannotResolveMethod3() throws Throwable {
    doTest("The FUNCTION 'add' is not defined");
  }

  public void testCannotResolveMethodInClass1() throws Throwable {
    doTest("The method 'bar' is not defined for the class 'Foo'");
  }

  public void testFieldDoesNotHaveAGetter1() throws Throwable {
    doTest("There is no such getter 'foo' in 'A'");
  }

  public void testFieldDoesNotHaveASetter1() throws Throwable {
    doTest("There is no such setter 'foo' in 'A'");
  }

  public void NotAStaticField() throws Throwable {
    // https://code.google.com/p/dart/issues/detail?id=10754
    doTest("\"bar\" is not a static field");
  }

  public void NotAStaticMethod() throws Throwable {
    // https://code.google.com/p/dart/issues/detail?id=10754
    doTest("\"bar\" is not a static method");
  }
}
