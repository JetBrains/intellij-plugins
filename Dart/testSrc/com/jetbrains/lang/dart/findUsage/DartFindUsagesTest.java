package com.jetbrains.lang.dart.findUsage;

/**
 * @author: Fedor.Korotkov
 */
public class DartFindUsagesTest extends DartFindUsagesTestBase {
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

  public void testLibrary2() throws Throwable {
    doTest(1, "Library2Foo.dart", "Library2.dart", "Library2Bar.dart");
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

  public void testType2() throws Throwable {
    doTest(2, "Type2.dart", "Type2Library.dart");
  }

  public void testTypeInExtends1() throws Throwable {
    doTest(1);
  }

  public void testTypeInExtends2() throws Throwable {
    doTest(1, "TypeInExtends2.dart", "TypeInExtends2Bar.dart", "TypeInExtends2Library.dart");
  }
}
