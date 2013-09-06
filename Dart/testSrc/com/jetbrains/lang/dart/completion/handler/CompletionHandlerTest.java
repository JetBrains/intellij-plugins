package com.jetbrains.lang.dart.completion.handler;

import com.intellij.codeInsight.completion.CompletionType;

/**
 * @author: Fedor.Korotkov
 */
public class CompletionHandlerTest extends CompletionHandlerTestBase {
  @Override
  protected String getBasePath() {
    return "/completion/handler";
  }

  @Override
  protected String getTestFileExtension() {
    return ".dart";
  }

  public void testConstructor1() throws Throwable {
    doTest();
  }

  public void testConstructor2() throws Throwable {
    doTest();
  }

  public void testConstructor3() throws Throwable {
    doTest();
  }

  public void testImport1() throws Throwable {
    myFixture.copyFileToProject("additional/Foo.dart");
    myFixture.copyFileToProject("additional/FooPart.dart");
    doTest(CompletionType.BASIC, 2, "Foo");
  }

  public void testImport2() throws Throwable {
    myFixture.copyFileToProject("additional/Foo.dart");
    myFixture.copyFileToProject("additional/FooPart.dart");
    doTest(CompletionType.BASIC, 2, "FooPart");
  }

  public void testImport3() throws Throwable {
    myFixture.copyFileToProject("additional/Foo.dart");
    myFixture.copyFileToProject("additional/FooPart.dart");
    doTest(CompletionType.BASIC, 2, "Foo");
  }

  public void testImport4() throws Throwable {
    myFixture.copyFileToProject("additional/Foo.dart");
    myFixture.copyFileToProject("additional/FooPart.dart");
    doTest(CompletionType.BASIC, 2, "Foo");
  }

  public void testImport5part() throws Throwable {
    myFixture.copyFileToProject("additional/Foo.dart");
    myFixture.copyFileToProject("additional/FooPart.dart");
    myFixture.copyFileToProject("import5.dart");
    doTest(CompletionType.BASIC, 2, "Foo");
    myFixture.checkResultByFile("import5.dart", "import5.after.dart", false);
  }

  public void testParentheses1() throws Throwable {
    doTest();
  }

  public void testParentheses2() throws Throwable {
    doTest();
  }
}
