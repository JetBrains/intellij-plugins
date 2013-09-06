package com.jetbrains.lang.dart.resolve;

import com.jetbrains.lang.dart.util.DartHtmlUtil;
import com.jetbrains.lang.dart.util.DartSdkTestUtil;

import java.io.IOException;

/**
 * Part of DartGotoDeclarationActionTest tests in script block
 *
 * @author: Fedor.Korotkov
 */
public class DartGotoInHtmlDeclarationActionTest extends DartGotoDeclarationActionTestBase {

  protected void doTest(int expectedSize, String... files) throws IOException {
    doTest(DartHtmlUtil.createHtmlAndConfigureFixture(myFixture, files), expectedSize);
  }

  public void testArrayAccess() throws Throwable {
    doTestWithSDK(1);
  }

  public void testCascade1() throws Throwable {
    doTest(1);
  }

  public void testCascade2() throws Throwable {
    doTest(1);
  }

  public void testCascade3() throws Throwable {
    doTest(1);
  }

  public void testCascade4() throws Throwable {
    doTest(1);
  }

  public void testCascade5() throws Throwable {
    doTest(1);
  }

  public void testForLoop1() throws Throwable {
    doTest(1);
  }

  public void testForLoop2() throws Throwable {
    doTest(1);
  }

  public void testForLoop3() throws Throwable {
    doTest(1);
  }

  public void testFunctionParameter() throws Throwable {
    doTest(1);
  }

  public void testFunctionParameter1() throws Throwable {
    doTest(1);
  }

  public void testFunctionParameter2() throws Throwable {
    doTest(1);
  }

  public void testFunctionParameter3() throws Throwable {
    doTest(1);
  }

  public void testFunctionParameter4() throws Throwable {
    doTest(1);
  }

  public void testGeneric1() throws Throwable {
    doTestWithSDK(1);
  }

  public void testGeneric2() throws Throwable {
    doTestWithSDK(1);
  }

  public void testGeneric3() throws Throwable {
    doTest(1);
  }

  public void testGeneric4() throws Throwable {
    doTestWithSDK(1);
  }

  public void testGeneric5() throws Throwable {
    doTestWithSDK(1);
  }

  public void testGeneric6() throws Throwable {
    doTestWithSDK(1);
  }

  public void testGeneric7() throws Throwable {
    doTestWithSDK(1);
  }

  public void testGeneric8() throws Throwable {
    doTestWithSDK(1);
  }

  public void testGeneric9() throws Throwable {
    doTestWithSDK(1);
  }

  public void testGeneric10() throws Throwable {
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

  public void testLibrary4() throws Throwable {
    doTestWithSDK(1);
  }

  public void testLibrary5() throws Throwable {
    doTest(1, "Library5.dart", "Library5Helper.dart");
  }

  public void testLibrary6() throws Throwable {
    doTest(1, "Library6.dart", "Library6Helper.dart");
  }

  public void testLibrary7() throws Throwable {
    doTest(1, "Library7.dart", "Library6Helper.dart");
  }

  public void testLibrary8() throws Throwable {
    doTest(1, "Library8.dart", "packages/foo/Foo.dart", "packages/bar/Bar.dart");
  }

  public void testLibrary9() throws Throwable {
    doTest(0, "Library9.dart", "packages/foo/Foo.dart", "packages/bar/Bar.dart");
  }

  public void testLibrary10() throws Throwable {
    myFixture.addFileToProject("pubspec.yaml", "");
    doTest(1, "Library10.dart", "packages/foo/Foo.dart", "packages/bar/Bar.dart");
  }

  public void testLibrary11() throws Throwable {
    doTest(1, "Library11.dart", "packages/foo/Foo.dart", "packages/bar/Bar.dart");
  }

  public void testLibrary12() throws Throwable {
    myFixture.addFileToProject("pubspec.yaml", "");
    doTest(1, "Library12.dart", "packages/foo/Foo.dart", "packages/bar/Bar.dart");
  }

  public void testMixin1() throws Throwable {
    doTest(1);
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

  public void testLocalVarDeclaration4() throws Throwable {
    doTest(0);
  }

  public void testOperator1() throws Throwable {
    doTestWithSDK(1);
  }

  public void testOperator2() throws Throwable {
    doTestWithSDK(1);
  }

  public void testOperator3() throws Throwable {
    doTestWithSDK(1);
  }

  public void testOperator4() throws Throwable {
    doTestWithSDK(1);
  }

  public void testPackage1() throws Throwable {
    myFixture.addFileToProject("pubspec.yaml", "");
    doTest(1, "Package1.dart", "packages/foo/Foo.dart", "packages/bar/Bar.dart");
  }

  public void testPackage2() throws Throwable {
    myFixture.addFileToProject("pubspec.yaml", "");
    doTest(1, "Package2.dart", "packages/foo/Foo.dart", "packages/bar/Bar.dart");
  }

  public void testPackage3() throws Throwable {
    doTest(0, "Package3.dart", "packages/foo/Foo.dart", "packages/bar/Bar.dart");
  }

  public void testPackage4() throws Throwable {
    doTest(1, "Package4.dart", "additional/foo_functions.dart");
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

  public void testReference4() throws Throwable {
    doTest(1);
  }

  public void testReference5() throws Throwable {
    doTest(1);
  }

  public void testReference6() throws Throwable {
    doTest(1);
  }

  public void testReference7() throws Throwable {
    doTest(1, "Reference7.dart", "Reference7Helper.dart");
  }

  public void testReference8() throws Throwable {
    doTest(1, "Reference8.dart", "Reference8Helper.dart");
  }

  public void testReference9() throws Throwable {
    doTest(1);
  }

  public void testReference10() throws Throwable {
    doTest(1);
  }

  public void testReference11() throws Throwable {
    doTest(0);
  }

  public void testReference12() throws Throwable {
    doTest(1);
  }

  public void testReference13() throws Throwable {
    doTestWithSDK(1);
  }

  public void testReference14() throws Throwable {
    doTestWithSDK(1);
  }

  public void testReference15() throws Throwable {
    doTestWithSDK(1);
  }

  public void testReference16() throws Throwable {
    doTest(1);
  }

  public void testReference17() throws Throwable {
    doTestWithSDK(1);
  }

  public void testReference18() throws Throwable {
    doTestWithSDK(1);
  }

  public void testReference19() throws Throwable {
    doTestWithSDK(1);
  }

  public void testReference20() throws Throwable {
    doTest(1);
  }

  public void testReference21() throws Throwable {
    doTestWithSDK(1);
  }

  public void testReference22() throws Throwable {
    doTestWithSDK(1);
  }

  public void testReference23() throws Throwable {
    doTest(1);
  }

  public void testReference24() throws Throwable {
    DartSdkTestUtil.configFakeSdk(myFixture);
    doTest(1);
  }

  public void testReference25() throws Throwable {
    doTestWithSDK(1);
  }

  public void testReference26() throws Throwable {
    doTestWithSDK(1);
  }

  public void testReference27() throws Throwable {
    doTest(1, "Reference27.dart", "Reference27Helper.dart");
  }

  public void testReference28() throws Throwable {
    doTestWithSDK(1);
  }

  public void testReference29() throws Throwable {
    doTest(1);
  }

  public void testReference30() throws Throwable {
    doTest(0);
  }

  public void testReference31() throws Throwable {
    doTest(0);
  }

  public void testReference32() throws Throwable {
    doTest(1);
  }

  public void testReference33() throws Throwable {
    doTest(1);
  }

  public void testReference34() throws Throwable {
    doTest(1);
  }

  public void testReference35() throws Throwable {
    doTest(0);
  }

  public void testReference36() throws Throwable {
    doTest(1);
  }

  public void testReference37() throws Throwable {
    doTest(1);
  }

  public void testReference38() throws Throwable {
    doTestWithSDK(1);
  }

  public void testType1() throws Throwable {
    doTest(1);
  }

  public void testType3() throws Throwable {
    doTest(1);
  }

  public void testTypedef1() throws Throwable {
    doTestWithSDK(1);
  }

  public void testTypeInExtends1() throws Throwable {
    doTest(1);
  }

  public void testWi14225() throws Throwable {
    doTest(1);
  }
}
