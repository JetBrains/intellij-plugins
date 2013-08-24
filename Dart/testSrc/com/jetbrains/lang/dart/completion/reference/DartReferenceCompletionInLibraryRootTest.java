package com.jetbrains.lang.dart.completion.reference;

import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.completion.base.DartCompletionTestBase;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.jetbrains.lang.dart.util.DartSdkTestUtil;

/**
 * @author: Fedor.Korotkov
 */
public class DartReferenceCompletionInLibraryRootTest extends DartCompletionTestBase {
  public DartReferenceCompletionInLibraryRootTest() {
    super("completion", "references");
  }

  public void testCascade1() throws Throwable {
    doTest();
  }

  public void testCascade2() throws Throwable {
    doTest();
  }

  // WI-14473
  public void testLibrary1() throws Throwable {
    myFixture.addFileToProject("pubspec.yaml", "");
    myFixture.copyFileToProject("packages/foo/Foo.dart");
    myFixture.configureByText("library1.dart", "import 'package:foo/Foo.dart' show B<caret>");
    doTestVariantsInner(getTestName(false) + ".txt");
  }

  // WI-14468
  public void testLibrary2() throws Throwable {
    myFixture.configureByText("bar.dart", "library Bar;");
    myFixture.configureByText("baz.dart", "library Baz;");
    myFixture.configureByText("library2.dart", "part of B<caret>");
    doTestVariantsInner(getTestName(false) + ".txt");
  }

  public void testLibrary3() throws Throwable {
    myFixture.configureByText("bar.dart", "library dart.bar;");
    myFixture.configureByText("baz.dart", "library dart.baz;");
    myFixture.configureByText("library3.dart", "part of da<caret>");
    doTestVariantsInner(getTestName(false) + ".txt");
  }

  public void testMixin1() throws Throwable {
    doTest();
  }

  public void testMixin2() throws Throwable {
    doTest();
  }

  public void testPackages1() throws Throwable {
    myFixture.addFileToProject("pubspec.yaml", "");
    myFixture.addFileToProject("packages/foo/foo.dart", "");
    doTest();
  }

  public void testPackages2() throws Throwable {
    myFixture.addFileToProject("pubspec.yaml", "");
    myFixture.addFileToProject("packages/foo/foo.dart", "");
    doTest();
  }

  public void testPackages3() throws Throwable {
    myFixture.addFileToProject("packages/foo/foo.dart", "");
    doTest();
  }

  public void testPackages4() throws Throwable {
    myFixture.addFileToProject("pubspec.yaml", "");
    myFixture.addFileToProject("packages/foo/foo.dart", "");
    PsiFile psiFile = myFixture.addFileToProject("packages/my_app/main.dart", "#import('package:foo/<caret>');");
    myFixture.configureFromExistingVirtualFile(DartResolveUtil.getRealVirtualFile(psiFile));
    doTestVariantsInner(getTestName(false) + ".txt");
  }

  public void testPackages5() throws Throwable {
    myFixture.addFileToProject("pubspec.yaml", "");
    myFixture.addFileToProject("packages/foo/foo.dart", "");
    PsiFile psiFile = myFixture.addFileToProject("lib/main.dart", "import 'package:foo/<caret>'");
    myFixture.configureFromExistingVirtualFile(DartResolveUtil.getRealVirtualFile(psiFile));
    doTestVariantsInner(getTestName(false) + ".txt");
  }

  public void testPath1() throws Throwable {
    doTest();
  }

  public void testPath2() throws Throwable {
    myFixture.addFileToProject("foo/Bar.dart", "");
    myFixture.addFileToProject("foo/Baz.dart", "");
    doTest();
  }

  public void testReference1() throws Throwable {
    doTest();
  }

  public void testReference2() throws Throwable {
    doTest();
  }

  public void testReference3() throws Throwable {
    doTest();
  }

  public void testReference4() throws Throwable {
    doTest();
  }

  public void testReference5() throws Throwable {
    doTest();
  }

  public void testReference6() throws Throwable {
    doTest();
  }

  public void testReference7() throws Throwable {
    doTest();
  }

  public void testReference8() throws Throwable {
    doTest("Reference8.dart", "additional/foo.dart");
  }

  public void testReference9() throws Throwable {
    doTest("Reference9.dart", "additional/foo_functions.dart");
  }

  public void testReference10() throws Throwable {
    doTest("Reference10.dart", "additional/Bar.dart", "additional/bar_part.dart");
  }

  public void testReference11() throws Throwable {
    doTest("Reference11.dart", "additional/Bar.dart", "additional/bar_part.dart");
  }

  public void testReference12() throws Throwable {
    doTest("Reference12.dart", "Reference12Helper.dart", "additional/Bar.dart", "additional/bar_part.dart");
  }

  public void testReference13() throws Throwable {
    doTest("Reference13.dart", "Reference13Helper.dart", "additional/Bar.dart", "additional/bar_part.dart");
  }

  public void testReference14() throws Throwable {
    doTest("Reference14.dart", "additional/Bar.dart", "additional/bar_part.dart");
  }

  public void testReference15() throws Throwable {
    doTest("Reference15.dart", "additional/Bar.dart", "additional/bar_part.dart");
  }

  public void testReference16() throws Throwable {
    doTest("Reference16.dart", "additional/Bar.dart", "additional/bar_part.dart");
  }

  public void testReference17() throws Throwable {
    myFixture.addFileToProject("pubspec.yaml", "");
    doTest("Reference17.dart", "packages/foo/Foo.dart");
  }

  public void testReference18() throws Throwable {
    doTest("Reference18.dart", "packages/foo/Foo.dart");
  }

  public void testReference19() throws Throwable {
    doTest();
  }

  public void testReference20() throws Throwable {
    doTest();
  }

  public void testReference21() throws Throwable {
    doTest("Reference21.dart", "additional/Bar.dart", "additional/bar_part.dart");
  }

  public void testReference22() throws Throwable {
    doTest();
  }

  public void testReference23() throws Throwable {
    doTest();
  }

  public void testReference24() throws Throwable {
    doTest();
  }

  public void testReference25() throws Throwable {
    doTest();
  }

  public void testReference26() throws Throwable {
    doTest();
  }

  public void testReference27() throws Throwable {
    doTest();
  }

  public void testTest1() throws Throwable {
    doTest();
  }

  public void testTest2() throws Throwable {
    doTest();
  }

  public void testTest3() throws Throwable {
    doTest();
  }

  public void testTest4() throws Throwable {
    doTest();
  }

  public void testTest5() throws Throwable {
    doTest();
  }

  public void testTest6() throws Throwable {
    doTest("Test6.dart", "Test6Helper.dart");
  }

  public void testTest9() throws Throwable {
    doTest("Test9.dart", "Test9Helper.dart");
  }

  public void testTest10() throws Throwable {
    DartSdkTestUtil.configFakeSdk(myFixture, "../../sdk");
    doTest();
  }

  public void testWEB_6447() throws Throwable {
    DartSdkTestUtil.configFakeSdk(myFixture, "../../sdk");
    doTest();
  }

  public void testWEB_6480() throws Throwable {
    DartSdkTestUtil.configFakeSdk(myFixture, "../../sdk");
    doTest();
  }

  public void testWEB_8100() throws Throwable {
    myFixture.addFileToProject("pubspec.yaml", "");
    doTest("WEB_8100.dart", "packages/foo/Foo.dart");
  }
}
