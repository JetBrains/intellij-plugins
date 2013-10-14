package com.jetbrains.lang.dart.analyzer;

public class DartResolverErrorAnalyzingTest extends DartAnalyzerTestBase {
  @Override
  protected String getBasePath() {
    return "/analyzer/resolver";
  }

  public void testCannotResolveMethod1() throws Throwable {
    doTest("The function 'foo' is not defined");
  }

  public void testCannotResolveMethod2() throws Throwable {
    doTest("The function 'add' is not defined");
  }

  public void testCannotResolveMethod3() throws Throwable {
    doTest("The function 'add' is not defined");
  }

  public void testCannotResolveMethod4$DartImportFix() throws Throwable {
    doTest("The function 'superPrint' is not defined", "cannotResolveMethod4PrinterLibrary.dart");
  }

  public void testCannotResolveMethodInClass1() throws Throwable {
    doTest("The method 'bar' is not defined for the class 'Foo'");
  }

  public void testFieldDoesNotHaveAGetter1() throws Throwable {
    doTest("There is no such getter 'foo' in 'A'");
  }

  // fails because analyzer now reports this error as "Final variables cannot be assigned a value"
  public void _testFieldDoesNotHaveASetter1() throws Throwable {
    doTest("There is no such setter 'foo' in 'A'");
  }

  public void testNotAStaticField() throws Throwable {
    doTest("Instance member 'bar' cannot be accessed using static access");
  }

  public void testNotAStaticMethod() throws Throwable {
    doTest("Instance member 'bar' cannot be accessed using static access");
  }
}
