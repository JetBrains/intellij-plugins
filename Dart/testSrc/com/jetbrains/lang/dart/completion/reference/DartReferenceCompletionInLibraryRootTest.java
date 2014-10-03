package com.jetbrains.lang.dart.completion.reference;

import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.jetbrains.lang.dart.completion.base.DartCompletionTestBase;
import com.jetbrains.lang.dart.util.DartResolveUtil;

public class DartReferenceCompletionInLibraryRootTest extends DartCompletionTestBase {
  public DartReferenceCompletionInLibraryRootTest() {
    super("completion", "references");
  }

  private void doTestSdkClassesInCompletion(final String text) throws Throwable {
    myFixture.configureByText("foo.dart", text);
    doTestVariantsInner("SdkClassesInCompletion.txt");
  }

  private void doTestNoSdkClassesInCompletion(final String text) throws Throwable {
    myFixture.configureByText("foo.dart", text);
    doTestVariantsInner("NoSdkClassesInCompletion.txt");
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
    PsiFile psiFile = myFixture.addFileToProject("my_app/main.dart", "import 'package:foo/<caret>';");
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
    doTest("Reference8.dart", "additional/Foo.dart");
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
    doTest();
  }

  public void testWEB_6447() throws Throwable {
    doTest();
  }

  public void testWEB_6480() throws Throwable {
    doTest();
  }

  public void testWEB_7963() throws Throwable {
    doTest();
  }

  public void testWEB_8100() throws Throwable {
    myFixture.addFileToProject("pubspec.yaml", "");
    doTest("WEB_8100.dart", "packages/foo/Foo.dart");
  }

  public void testTopLevelType() throws Throwable {
    doTestSdkClassesInCompletion("<caret>");
    doTestSdkClassesInCompletion("@<caret>");
    doTestSdkClassesInCompletion("@Object @<caret>");
    doTestSdkClassesInCompletion("@Object <caret>");
    doTestSdkClassesInCompletion("static <caret>");
    doTestSdkClassesInCompletion("final <caret>");
    doTestSdkClassesInCompletion("const <caret>");
    doTestSdkClassesInCompletion("@Object static <caret>");
    doTestSdkClassesInCompletion("@Object final <caret>");
    doTestSdkClassesInCompletion("@Object const <caret>");
    doTestNoSdkClassesInCompletion("Foo <caret>");
    doTestNoSdkClassesInCompletion("@Object Foo <caret>");
    doTestNoSdkClassesInCompletion("static Foo <caret>");
    doTestNoSdkClassesInCompletion("final Foo <caret>");
    doTestNoSdkClassesInCompletion("const Foo <caret>");
  }

  public void testClassLevelType() throws Throwable {
    doTestSdkClassesInCompletion("class Foo extends <caret>");
    doTestSdkClassesInCompletion("class Foo implements <caret>");
    doTestSdkClassesInCompletion("class Foo extends Object implements <caret>");
    doTestSdkClassesInCompletion("class Foo = <caret>");
    doTestSdkClassesInCompletion("class Foo {<caret>}");
    doTestSdkClassesInCompletion("class Foo {@<caret>}");
    doTestSdkClassesInCompletion("class Foo {@Object @<caret>}");
    doTestSdkClassesInCompletion("class Foo {@Object <caret>}");
    doTestSdkClassesInCompletion("class Foo {static <caret>}");
    doTestSdkClassesInCompletion("class Foo {final <caret>}");
    doTestSdkClassesInCompletion("class Foo {const <caret>}");
    doTestSdkClassesInCompletion("class Foo {@Object static <caret>}");
    doTestSdkClassesInCompletion("class Foo {@Object final <caret>}");
    doTestSdkClassesInCompletion("class Foo {@Object const <caret>}");
    doTestNoSdkClassesInCompletion("class Foo{Foo <caret>");
    doTestNoSdkClassesInCompletion("class Foo{@Object Foo <caret>");
    doTestNoSdkClassesInCompletion("class Foo{static Foo <caret>}");
    doTestNoSdkClassesInCompletion("class Foo{final Foo <caret>}");
    doTestNoSdkClassesInCompletion("class Foo{const Foo <caret>}");
  }

  public void testExceptionParameter1() throws Throwable {
    doTest();
  }

  public void testExceptionParameter2() throws Throwable {
    doTest();
  }

  public void testExceptionParameter3() throws Throwable {
    doTest();
  }

  public void testExceptionParameter4() throws Throwable {
    doTest();
  }

  public void testPackageFolderCompletionInHtml() throws Throwable {
    myFixture.addFileToProject("pubspec.yaml", "");
    myFixture.addFileToProject("packages/browser/dart.js", "");
    myFixture.addFileToProject("web/other.dart", "");
    final PsiFile psiFile = myFixture.addFileToProject("web/file.html", "<link href=''>");
    myFixture.openFileInEditor(psiFile.getVirtualFile());
    myFixture.getEditor().getCaretModel().moveToOffset(12);

    doTestVariantsInner(getTestName(false) + ".txt");
  }

  public void testLivePackageNameCompletionInHtml() throws Throwable {
    myFixture.addFileToProject("pubspec.yaml", "name: ProjectName\n" +
                                               "dependencies:\n" +
                                               "  PathPackage:\n" +
                                               "    path: local_package\n");
    myFixture.addFileToProject("lib/projectFile.dart", "");
    myFixture.addFileToProject("local_package/lib/localPackageFile.html", "");
    myFixture.addFileToProject("packages/browser/dart.js", "");
    final PsiFile psiFile = myFixture.addFileToProject("web/file.html", "<link href='packages/'>");
    myFixture.openFileInEditor(psiFile.getVirtualFile());
    myFixture.getEditor().getCaretModel().moveToOffset(21);

    doTestVariantsInner(getTestName(false) + ".txt");
  }

  public void testLivePackageContentCompletionInHtml() throws Throwable {
    myFixture.addFileToProject("pubspec.yaml", "name: ProjectName\n");
    myFixture.addFileToProject("lib/projectFile.dart", "");
    final PsiFile psiFile = myFixture.addFileToProject("web/file.html", "<link href='packages/ProjectName/xxx'>");
    myFixture.openFileInEditor(psiFile.getVirtualFile());
    myFixture.getEditor().getCaretModel().moveToOffset(33);

    doTestVariantsInner(getTestName(false) + ".txt");
  }
}
