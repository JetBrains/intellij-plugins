package com.jetbrains.lang.dart.analyzer;

import com.google.dart.engine.context.AnalysisContext;
import com.google.dart.engine.error.AnalysisError;
import com.intellij.openapi.util.Pair;

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

  public void testNoDartInHtml() throws Throwable {
    myFixture.configureByFiles(getTestName(true) + ".html");
    assertNull(new DartInProcessAnnotator().collectInformation(myFixture.getFile()));
  }

  public void testNoErrorsInHtml() throws Throwable {
    final String testName = getTestName(true);
    myFixture.configureByFiles(testName + ".html", testName + "_2.dart");
    final AnalysisError[] errors = getErrorsFromAnnotator();
    assertEmpty(errors);
  }

  public void testPartInSubfolder() throws Throwable {
    final String testName = getTestName(true);
    myFixture.configureByFiles(testName + ".dart", "subfolder/" + testName + "_part.dart");
    final AnalysisError[] errors = getErrorsFromAnnotator();
    assertEmpty(errors);
  }

  public void testCreateMethodInHtml() throws Throwable {
    doTest("The function 'bar' is not defined");
  }
}
