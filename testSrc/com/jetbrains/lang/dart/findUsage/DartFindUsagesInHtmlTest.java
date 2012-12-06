package com.jetbrains.lang.dart.findUsage;

import com.jetbrains.lang.dart.util.DartHtmlUtil;

/**
 * @author: Fedor.Korotkov
 */
public class DartFindUsagesInHtmlTest extends DartFindUsagesTestBase {
  @Override
  protected void doTest(int size, String... files) throws Throwable {
    DartHtmlUtil.createHtmlAndConfigureFixture(myFixture, files);
    doTestInner(size);
  }

  public void testForLoop1() throws Throwable {
    doTest(3);
  }

  public void testForLoop2() throws Throwable {
    doTest(1);
  }

  public void testFunctionParameter() throws Throwable {
    doTest(1);
  }

  public void testFunctionParameter1() throws Throwable {
    doTest(2);
  }

  public void testFunctionParameter2() throws Throwable {
    doTest(2);
  }

  public void testFunctionParameter3() throws Throwable {
    doTest(1);
  }

  public void testGlobalFunction() throws Throwable {
    doTest(1);
  }

  public void testLibrary1() throws Throwable {
    doTest(1, "Library1.dart", "Library1Helper.dart");
  }

  public void testLibrary3() throws Throwable {
    doTest(1, "Library3.dart", "Library3Foo.dart");
  }

  public void testLocalVarDeclaration1() throws Throwable {
    doTest(1);
  }

  public void testLocalVarDeclaration2() throws Throwable {
    doTest(1);
  }

  public void testLocalVarDeclaration3() throws Throwable {
    doTest(1);
  }

  public void testReference1() throws Throwable {
    doTest(1);
  }

  public void testReference2() throws Throwable {
    doTest(1);
  }

  public void testReference3() throws Throwable {
    doTest(1);
  }

  public void testType1() throws Throwable {
    doTest(1);
  }

  public void testTypeInExtends1() throws Throwable {
    doTest(1);
  }
}
