package com.jetbrains.lang.dart.resolve;

import com.intellij.codeInsight.TargetElementUtilBase;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.file.PsiDirectoryImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.DartProjectComponent;
import com.jetbrains.lang.dart.ide.index.DartLibraryIndex;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.util.CaretPositionInfo;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DartResolveTest extends DartCodeInsightFixtureTestCase {

  private void doTest(@NotNull final String text) {
    myFixture.configureByText("file.dart", text);
    doTest();
  }

  private void doTest() {
    final List<CaretPositionInfo> caretPositions = DartTestUtils.extractPositionMarkers(getProject(), myFixture.getEditor().getDocument());

    for (CaretPositionInfo caretPositionInfo : caretPositions) {
      final int line = myFixture.getEditor().getDocument().getLineNumber(caretPositionInfo.caretOffset);
      final int column = caretPositionInfo.caretOffset - myFixture.getEditor().getDocument().getLineStartOffset(line);
      final String fileNameAndPosition = myFixture.getFile().getName() + ":" + (line + 1) + ":" + (column + 1);

      final PsiReference reference = TargetElementUtilBase.findReference(myFixture.getEditor(), caretPositionInfo.caretOffset);
      assertNotNull("No reference in " + fileNameAndPosition, reference);

      final PsiElement resolve = reference.resolve();
      final String actualElementPosition = getPresentableElementPosition(resolve);
      assertEquals("Incorrect resolution for reference in " + fileNameAndPosition, caretPositionInfo.expected, actualElementPosition);
    }
  }

  @NotNull
  private static String getPresentableElementPosition(final @Nullable PsiElement element) {
    if (element == null) return "";

    final StringBuilder buf = new StringBuilder(element.getText());
    DartComponent component = PsiTreeUtil.getParentOfType(element, DartComponent.class);
    while (component != null) {
      final DartComponentName componentName = component.getComponentName();
      if (componentName != null && componentName != element) {
        buf.insert(0, component.getName() + " -> ");
      }
      component = PsiTreeUtil.getParentOfType(component, DartComponent.class);
    }
    String path = element instanceof PsiDirectoryImpl ? ((PsiDirectoryImpl)element).getVirtualFile().getPath()
                                                      : element.getContainingFile().getVirtualFile().getPath();
    if (path.startsWith("/src/")) path = path.substring("/src/".length());
    if (path.startsWith(DartTestUtils.SDK_HOME_PATH)) path = "[Dart SDK]" + path.substring(DartTestUtils.SDK_HOME_PATH.length());
    if (buf.length() > 0) buf.insert(0, " -> ");
    buf.insert(0, path);

    return buf.toString();
  }

  public void testResolveScope() throws Exception {
    try {
      final VirtualFile inSdk1 = DartLibraryIndex.getSdkLibByUri(getProject(), "dart:collection");
      final VirtualFile inSdk2 = DartLibraryIndex.getSdkLibByUri(getProject(), "dart:math");

      final VirtualFile inIdeLib1 = myFixture.addFileToProject("library/inLibrary1.dart", "").getVirtualFile();
      final VirtualFile inIdeLib2 = myFixture.addFileToProject("library/inLibrary2.dart", "").getVirtualFile();
      configureLibrary(inIdeLib1.getParent());

      final VirtualFile inContent = myFixture.addFileToProject("inContentOutsideDartRoot.dart", "").getVirtualFile();

      myFixture.addFileToProject("DartProject2/pubspec.yaml", "name: ProjectName2\n");
      final VirtualFile inProject2Web = myFixture.addFileToProject("DartProject2/web/inProject2Web.dart", "").getVirtualFile();
      final VirtualFile inProject2Lib = myFixture.addFileToProject("DartProject2/lib/inProject2Lib.dart", "").getVirtualFile();

      final VirtualFile pubspec = myFixture.addFileToProject("DartProject1/pubspec.yaml", "name: ProjectName1\n" +
                                                                                          "dependencies:\n" +
                                                                                          "  PathPackage:\n" +
                                                                                          "    path: ../DartProject2\n").getVirtualFile();
      final VirtualFile inProject1Root = myFixture.addFileToProject("DartProject1/inProject1Root.dart", "").getVirtualFile();
      final VirtualFile inLib = myFixture.addFileToProject("DartProject1/lib/inLib.dart", "").getVirtualFile();
      final VirtualFile inPackages = myFixture.addFileToProject("DartProject1/packages/inPackages.dart", "").getVirtualFile();
      final VirtualFile inWeb = myFixture.addFileToProject("DartProject1/web/inWeb.dart", "").getVirtualFile();
      final VirtualFile inWebSub = myFixture.addFileToProject("DartProject1/web/sub/inWebSub.dart", "").getVirtualFile();
      final VirtualFile inExcluded = myFixture.addFileToProject("DartProject1/web/packages/inExcluded.dart", "").getVirtualFile();
      final VirtualFile inTest = myFixture.addFileToProject("DartProject1/test/inTest.dart", "").getVirtualFile();
      final VirtualFile inExample = myFixture.addFileToProject("DartProject1/example/inExample.dart", "").getVirtualFile();

      DartProjectComponent.excludeBuildAndPackagesFolders(myModule, pubspec);

      doTestDartScope(inExcluded, null, null, true);
      doTestDartScope(new VirtualFile[]{inSdk1, inSdk2},
                      new VirtualFile[]{inSdk1, inSdk2},
                      new VirtualFile[]{inIdeLib1, inIdeLib2, inContent, inProject2Web, inProject2Lib,
                        inProject1Root, inLib, inPackages, inWeb, inWebSub, inExcluded, inTest, inExample});
      doTestDartScope(new VirtualFile[]{inIdeLib1, inIdeLib2},
                      new VirtualFile[]{inSdk1, inSdk2, inIdeLib1, inIdeLib2},
                      new VirtualFile[]{inContent, inProject2Web, inProject2Lib,
                        inProject1Root, inLib, inPackages, inWeb, inWebSub, inExcluded, inTest, inExample});
      doTestDartScope(new VirtualFile[]{inContent},
                      new VirtualFile[]{inSdk1, inSdk2, inIdeLib1, inIdeLib2, inContent, inProject2Web, inProject2Lib,
                        inProject1Root, inLib, inPackages, inWeb, inWebSub, inTest, inExample},
                      new VirtualFile[]{inExcluded});
      doTestDartScope(inLib,
                      new VirtualFile[]{inSdk1, inSdk2, inIdeLib1, inIdeLib2, inProject2Lib, inLib, inPackages},
                      new VirtualFile[]{inContent, inProject2Web, inExcluded, inProject1Root, inWeb, inWebSub, inTest, inExample},
                      true);
      doTestDartScope(new VirtualFile[]{inPackages},
                      new VirtualFile[]{inSdk1, inSdk2, inIdeLib1, inIdeLib2, inProject2Lib, inLib, inPackages},
                      new VirtualFile[]{inContent, inProject2Web, inExcluded, inProject1Root, inWeb, inWebSub, inTest, inExample});
      doTestDartScope(new VirtualFile[]{inWeb, inWebSub},
                      new VirtualFile[]{inSdk1, inSdk2, inIdeLib1, inIdeLib2, inProject2Lib, inLib, inPackages, inWeb, inWebSub},
                      new VirtualFile[]{inContent, inProject2Web, inExcluded, inProject1Root, inTest, inExample},
                      true);
      doTestDartScope(inExample,
                      new VirtualFile[]{inSdk1, inSdk2, inIdeLib1, inIdeLib2, inProject2Lib, inLib, inPackages, inExample},
                      new VirtualFile[]{inContent, inProject2Web, inExcluded, inProject1Root, inTest, inWeb, inWebSub},
                      true);
      doTestDartScope(new VirtualFile[]{inProject1Root, inTest},
                      new VirtualFile[]{inSdk1, inSdk2, inIdeLib1, inIdeLib2, inProject2Lib,
                        inProject1Root, inLib, inPackages, inWeb, inWebSub, inTest, inExample},
                      new VirtualFile[]{inContent, inProject2Web, inExcluded},
                      true);
      doTestDartScope(new VirtualFile[]{inProject1Root, inLib, inWeb, inWebSub, inTest, inExample},
                      new VirtualFile[]{inSdk1, inSdk2, inIdeLib1, inIdeLib2, inProject2Lib,
                        inProject1Root, inLib, inPackages, inWeb, inWebSub, inTest, inExample},
                      new VirtualFile[]{inContent, inProject2Web, inExcluded},
                      false);
    }
    finally {
      DartTestUtils.resetModuleRoots(myModule);
    }
  }

  private void doTestDartScope(final VirtualFile[] contextFiles,
                               final VirtualFile[] expectedInScope,
                               final VirtualFile[] expectedOutsideScope) {
    for (VirtualFile file : contextFiles) {
      doTestDartScope(file, expectedInScope, expectedOutsideScope, true);
      doTestDartScope(file, expectedInScope, expectedOutsideScope, false);
    }
  }

  private void doTestDartScope(final VirtualFile[] contextFiles,
                               final VirtualFile[] expectedInScope,
                               final VirtualFile[] expectedOutsideScope,
                               final boolean strictScope) {
    for (VirtualFile file : contextFiles) {
      doTestDartScope(file, expectedInScope, expectedOutsideScope, strictScope);
    }
  }

  private void doTestDartScope(final VirtualFile contextFile,
                               final VirtualFile[] expectedInScope,
                               final VirtualFile[] expectedOutsideScope,
                               final boolean strictScope) {
    final GlobalSearchScope scope = DartResolveScopeProvider.getDartScope(getProject(), contextFile, strictScope);

    if (scope == null) {
      assertTrue("Null scope not expected for " + contextFile.getPath(), expectedInScope == null);
      return;
    }

    if (expectedInScope == null) {
      fail("Null scope expected for " + contextFile.getPath());
      return;
    }

    for (VirtualFile file : expectedInScope) {
      assertTrue("Expected to be in scope: " + file.getPath(), scope.contains(file));
    }

    for (VirtualFile file : expectedOutsideScope) {
      assertFalse("Expected to be out of scope: " + file.getPath(), scope.contains(file));
    }
  }

  private void configureLibrary(final VirtualFile root) {
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        final ModifiableRootModel model = ModuleRootManager.getInstance(myModule).getModifiableModel();
        final Library library = model.getModuleLibraryTable().createLibrary();
        final Library.ModifiableModel libModel = library.getModifiableModel();
        libModel.addRoot(root, OrderRootType.CLASSES);
        libModel.commit();
        model.commit();
      }
    });
  }

  public void testNoRecursiveImports() throws Exception {
    myFixture.addFileToProject("file2.dart", "inFile2(){}");
    myFixture.addFileToProject("file1.dart", "import 'file2.dart'\n inFile1(){}");
    myFixture.addFileToProject("file.dart", "library fileLib;\n" +
                                            "import 'file1.dart';\n" +
                                            "part 'filePart1.dart';\n" +
                                            "part 'filePart2.dart';\n" +
                                            "inFile(){}");
    myFixture.addFileToProject("filePart1.dart", "part of fileLib;\n" +
                                                 "inFilePart1(){}");
    myFixture.configureByText("filePart2.dart", "part of fileLib;\n" +
                                                "inFilePart2(){\n" +
                                                "  <caret expected='filePart1.dart -> inFilePart1'>inFilePart1()\n" +
                                                "  <caret expected='filePart2.dart -> inFilePart2'>inFilePart2()\n" +
                                                "  <caret expected='file.dart -> inFile'>inFile()\n" +
                                                "  <caret expected='file1.dart -> inFile1'>inFile1()\n" +
                                                "  <caret expected=''>inFile2()\n" +
                                                "}");
    doTest();
  }

  public void testResolveWithExports() throws Exception {
    myFixture.addFileToProject("file1.dart", "inFile1(){}\n" +
                                             "inFile1HiddenLater(){}");
    myFixture.addFileToProject("file2.dart", "inFile2(){}");
    myFixture.addFileToProject("file3.dart", "inFile3(){}");
    myFixture.addFileToProject("file4.dart", "inFile4(){}");
    myFixture.addFileToProject("file5.dart", "inFile5(){}");
    myFixture.addFileToProject("file6.dart", "export 'file1.dart;\n" +
                                             "export 'file2.dart' show xxx, inFile2, yyy;\n" +
                                             "export 'file3.dart' show xxx, yyy;\n" +
                                             "export 'file4.dart' hide xxx, inFile4, yyy;\n" +
                                             "export 'file5.dart' hide xxx, yyy;\n" +
                                             "export 'file1.dart';\n" +
                                             "export 'file.dart';\n" +
                                             "inFile6(){}");
    myFixture.addFileToProject("file7.dart", "export 'file6.dart' hide inFile1HiddenLater;\n" +
                                             "inFile7(){}");
    myFixture.addFileToProject("file8.dart", "inFile8(){}");
    myFixture.addFileToProject("file.dart", "library fileLib;\n" +
                                            "import 'file7.dart';\n" +
                                            "export 'file8.dart';\n" +
                                            "part 'filePart1.dart';\n" +
                                            "inFile(){}");
    myFixture.configureByText("filePart1.dart", "part of fileLib;\n" +
                                                "foo(){\n" +
                                                "  <caret expected='file.dart -> inFile'>inFile()\n" +
                                                "  <caret expected=''>inFile8()\n" +
                                                "  <caret expected='file7.dart -> inFile7'>inFile7()\n" +
                                                "  <caret expected='file6.dart -> inFile6'>inFile6()\n" +
                                                "  <caret expected='file5.dart -> inFile5'>inFile5()\n" +
                                                "  <caret expected=''>inFile4()\n" +
                                                "  <caret expected=''>inFile3()\n" +
                                                "  <caret expected='file2.dart -> inFile2'>inFile2()\n" +
                                                "  <caret expected='file1.dart -> inFile1'>inFile1()\n" +
                                                "  <caret expected=''>inFile1HiddenLater()\n" +
                                                "}");
    doTest();
  }

  public void testFileReferencesInImports() throws Exception {
    myFixture.addFileToProject("pubspec.yaml", "name: ProjectName\n" +
                                               "dependencies:\n" +
                                               "  PathPackage:\n" +
                                               "    path: local_package\n");
    myFixture.addFileToProject("local_package/lib/localPackageLib1.dart", "");
    myFixture.addFileToProject("local_package/lib/src/localPackageLib2.dart", "");
    myFixture.addFileToProject("packages/ProjectName/src/file0.dart", ""); // symlink to lib/src/file0.dart
    myFixture.addFileToProject("packages/ProjectName/file1.dart", ""); // symlink to lib/file1.dart
    myFixture.addFileToProject("packages/ProjectName/file2.dart", ""); // symlink to lib/file2.dart
    myFixture.addFileToProject("packages/PathPackage/localPackageLib1.dart", ""); // symlink to local_package/lib/localPackageLib1.dart
    myFixture
      .addFileToProject("packages/PathPackage/src/localPackageLib2.dart", ""); // symlink to local_package/lib/src/localPackageLib2.dart
    myFixture.addFileToProject("packages/SomePackage/somePack1.dart", "");
    myFixture.addFileToProject("packages/SomePackage/src/somePack2.dart", "");
    myFixture.addFileToProject("lib/src/file0.dart", "");
    myFixture.addFileToProject("lib/file1.dart", "");
    final PsiFile psiFile = myFixture.addFileToProject(
      "lib/file2.dart",

      "import '<caret expected='lib/file1.dart'>file1.dart<caret expected='lib/file1.dart'>';\n" +
      "import '<caret expected='lib'>.<caret expected='lib'>/<caret expected='lib/file1.dart'>file1.dart<caret expected='lib/file1.dart'>';\n" +
      "import '<caret expected='/src'>..<caret expected='/src'>/<caret expected='lib'>lib<caret expected='lib'>/<caret expected='lib/file1.dart'>file1.dart<caret expected='lib/file1.dart'>';\n" +
      "import '<caret expected='lib/src'>src<caret expected='lib/src'>/<caret expected='lib/src/file0.dart'>file0.dart<caret expected='lib/src/file0.dart'>';\n" +
      // todo not sure that DartStringLiteralExpression should be here as reference
      "import 'package:ProjectName<caret expected=''>/<caret expected='lib/src'>src<caret expected='lib/src'>/<caret expected='lib/src/file0.dart'>file0.dart<caret expected='lib/src/file0.dart'>';\n" +
      "import 'package:ProjectName<caret expected=''>/<caret expected='lib/file1.dart'>file1.dart<caret expected='lib/file1.dart'>';\n" +
      "import 'package:PathPackage<caret expected=''>/<caret expected='local_package/lib/localPackageLib1.dart'>localPackageLib1.dart<caret expected='local_package/lib/localPackageLib1.dart'>';\n" +
      "import 'package:PathPackage<caret expected=''>/<caret expected='local_package/lib/src'>src<caret expected='local_package/lib/src'>/<caret expected='local_package/lib/src/localPackageLib2.dart'>localPackageLib2.dart<caret expected='local_package/lib/src/localPackageLib2.dart'>';\n" +
      "import 'package<caret expected=''>:<caret expected='packages/SomePackage'>SomePackage<caret expected='packages/SomePackage'>/<caret expected='packages/SomePackage/somePack1.dart'>somePack1.dart<caret expected='packages/SomePackage/somePack1.dart'>';\n" +
      "import 'package<caret expected=''>:<caret expected='packages/SomePackage'>SomePackage<caret expected='packages/SomePackage'>/<caret expected='packages/SomePackage/src'>src<caret expected='packages/SomePackage/src'>/<caret expected='packages/SomePackage/src/somePack2.dart'>somePack2.dart<caret expected='packages/SomePackage/src/somePack2.dart'>';\n" +
      ""
    );
    myFixture.openFileInEditor(psiFile.getVirtualFile());

    doTest();
  }

  public void testShowHideInImports() throws Exception {
    myFixture.addFileToProject("file1.dart", "foo1(){}\n" +
                                             "foo2(){}\n" +
                                             "foo3(){}\n" +
                                             "foo4(){}\n");
    doTest("import 'file1.dart' show foo1;\n" +
           "import 'file1.dart' show foo2;\n" +
           "import 'file1.dart' hide foo1, foo2, foo3, foo4;\n" +
           "import 'file1.dart' show foo3;\n" +
           "main(){\n" +
           "  <caret expected='file1.dart -> foo1'>foo1();\n" +
           "  <caret expected='file1.dart -> foo2'>foo2();\n" +
           "  <caret expected='file1.dart -> foo3'>foo3();\n" +
           "  <caret expected=''>foo4();\n" +
           "}");
  }

  public void testTransitiveShowHide() throws Exception {
    myFixture.addFileToProject("file1part.dart", "part of file1lib;\n" +
                                                 "var foo1, foo2, foo3, foo4, foo5, foo6, foo7, foo8, foo9;");
    myFixture.addFileToProject("file1.dart", "library file1lib;\n" +
                                             "part 'file1part.dart';");
    myFixture.addFileToProject("file2.dart", "export 'file1.dart' show foo1, foo2, foo3, foo4;");
    myFixture.addFileToProject("file3.dart", "export 'file1.dart' show foo5, foo6, foo7, foo8;");
    myFixture.addFileToProject("file4.dart", "export 'file2.dart' show foo1, foo2, foo3, foo5, foo6, foo7, foo8, foo9;");
    myFixture.addFileToProject("file5.dart", "export 'file3.dart' hide foo7, foo8;");
    myFixture.addFileToProject("file.dart", "library filelib;\n" +
                                            "import 'file4.dart' hide foo3;\n" +
                                            "import 'file5.dart' show foo1, foo2, foo3, foo4, foo5, foo7, foo8, foo9;\n" +
                                            "part 'filepart.dart';");
    myFixture.configureByText("filepart.dart", "part of filelib;\n" +
                                               "main(){\n" +
                                               "  <caret expected='file1part.dart -> foo1'>foo1;\n" +
                                               "  <caret expected='file1part.dart -> foo2'>foo2;\n" +
                                               "  <caret expected=''>foo3;\n" +
                                               "  <caret expected=''>foo4;\n" +
                                               "  <caret expected='file1part.dart -> foo5'>foo5;\n" +
                                               "  <caret expected=''>foo6;\n" +
                                               "  <caret expected=''>foo7;\n" +
                                               "  <caret expected=''>foo8;\n" +
                                               "  <caret expected=''>foo9;\n" +
                                               "}");
    doTest();
  }

  public void testMixinApplication() throws Exception {
    doTest("class Super  { inSuper(){}   }\n" +
           "class Mixin1 { inMixin1(){}  }\n" +
           "class Mixin2 { var inMixin2; }\n" +
           "class Inter1 { var inInter1; }\n" +
           "class Inter2 { inInter2(){}  }\n" +
           "class Foo = Super with Mixin1, Mixin2 implements Inter1, Inter2;\n" +
           "main(){\n" +
           "  var foo = new Foo();\n" +
           "  foo.<caret expected='file.dart -> Super -> inSuper'>inSuper();\n" +
           "  foo.inMixin1<caret expected='file.dart -> Mixin1 -> inMixin1'>();\n" +
           "  foo.inMixin2<caret expected='file.dart -> Mixin2 -> inMixin2'>;\n" +
           "  foo.inInter1<caret expected='file.dart -> Inter1 -> inInter1'>;\n" +
           "  foo.inInter2<caret expected='file.dart -> Inter2 -> inInter2'>();\n" +
           "}");
  }

  public void testExceptionParameters() throws Exception {
    doTest("main(){\n" +
           "  try{} on Error catch (e1){<caret expected='file.dart -> main -> e1'>e1;}\n" +
           "  try{} on Error catch (e2, s2){<caret expected='file.dart -> main -> e2'>e2 + <caret expected='file.dart -> main -> s2'>s2; <caret expected=''>e1;}\n" +
           "  try{} catch (e3){<caret expected='file.dart -> main -> e3'>e3; <caret expected=''>e1; <caret expected=''>e2; <caret expected=''>s2;}\n" +
           "  try{} catch (e4, s4){print(<caret expected='file.dart -> main -> e4'>e4 + <caret expected='file.dart -> main -> s4'>s4); <caret expected=''>e1; <caret expected=''>e2; <caret expected=''>s2; <caret expected=''>e3;}\n" +
           "}");
  }

  public void testObjectMembers() throws Exception {
    doTest("class Bar{}\n" +
           "class Foo extends Bar{\n" +
           "  String<caret expected='[Dart SDK]/lib/core/string.dart -> String'> toString(){\n" +
           "    var i = hashCode<caret expected='[Dart SDK]/lib/core/object.dart -> Object -> hashCode'>;\n" +
           "    var b = new Bar();\n" +
           "    b.runtimeType<caret expected='[Dart SDK]/lib/core/object.dart -> Object -> runtimeType'>;\n" +
           "    return super.toString<caret expected='[Dart SDK]/lib/core/object.dart -> Object -> toString'>();\n" +
           "  }\n" +
           "}");
  }

  public void testPackageReferencesInHtml() throws Exception {
    myFixture.addFileToProject("pubspec.yaml", "name: ProjectName\n" +
                                               "dependencies:\n" +
                                               "  PathPackage:\n" +
                                               "    path: local_package\n");
    myFixture.addFileToProject("lib/projectFile.dart", "");
    myFixture.addFileToProject("local_package/lib/localPackageFile.html", "");
    myFixture.addFileToProject("packages/browser/dart.js", "");
    final PsiFile psiFile = myFixture.addFileToProject("web/file.html",
                                                       "<script src='<caret expected='packages'>packages/<caret expected='lib'>ProjectName/<caret expected='lib/projectFile.dart'>projectFile.dart'/>\n" +
                                                       "<script src='packages<caret expected='packages'>/PathPackage<caret expected='local_package/lib'>/localPackageFile.html<caret expected='local_package/lib/localPackageFile.html'>'/>\n" +
                                                       "<script src='<caret expected='packages'>packages/<caret expected='packages/browser'>browser/<caret expected='packages/browser/dart.js'>dart.js'/>\n");
    myFixture.openFileInEditor(psiFile.getVirtualFile());
    doTest();
  }

  public void testCommentsInsideCallExpression() throws Exception {
    doTest("main(){\n" +
           "  '1 2 3 4'.<caret expected='[Dart SDK]/lib/core/string.dart -> String -> split'>split(' ')  // comment\n" +
           "  .map<caret expected='[Dart SDK]/lib/core/iterable.dart -> Iterable -> map'>((e) => int.<caret expected='[Dart SDK]/lib/core/int.dart -> int -> parse'>parse(e) * 2);\n" +
           "}");
  }

  public void testEnum() throws Exception {
    doTest("enum Foo {FooA, FooB, }\n" +
           "main() {\n" +
           "  print(<caret expected='file.dart -> Foo'>Foo.<caret expected='file.dart -> Foo -> FooB'>FooB);\n" +
           "}");
  }

  public void testPartViaPackageUrl() throws Exception {
    myFixture.addFileToProject("pubspec.yaml", "name: ProjectName\n");
    myFixture.addFileToProject("lib/lib.dart", "part 'package:ProjectName/part.dart';");
    myFixture.addFileToProject("lib/part.dart", "var foo;");
    doTest("import 'package:ProjectName/lib.dart';'\n" +
           "main() {\n" +
           "  var a = <caret expected='lib/part.dart -> foo'>foo;\n" +
           "}");
  }

  public void testFromPartToPartViaPackageUrl() throws Exception {
    myFixture.addFileToProject("pubspec.yaml", "name: ProjectName\n");
    myFixture.addFileToProject("lib/lib.dart", "library libName;\n" +
                                               "part 'package:ProjectName/part1.dart';\n" +
                                               "part 'package:ProjectName/part2.dart';");
    myFixture.addFileToProject("lib/part1.dart", "part of libName;" +
                                                 "var foo1;");
    final PsiFile psiFile = myFixture.addFileToProject("lib/part2.dart", "part of libName;\n" +
                                                                         "var foo2 = <caret expected='lib/part1.dart -> foo1'>foo1;");
    myFixture.openFileInEditor(psiFile.getVirtualFile());
    doTest();
  }

  public void testDartInternalLibrary() throws Exception {
    doTest("import 'dart:_internal';\n" +
           "class A extends <caret expected='[Dart SDK]/lib/internal/list.dart -> UnmodifiableListBase'>UnmodifiableListBase<E>{}");
  }

  public void testPartOfResolution() throws Exception {
    myFixture.addFileToProject("main1.dart", "library libName;\npart 'part1.dart';");
    myFixture.addFileToProject("main2.dart", "library libName;\npart 'part2.dart';");
    myFixture.configureByText("part1.dart", "part of <caret expected='main1.dart -> libName'>libName;");
    doTest();
  }

  public void testDuplicatedImportPrefix() throws Exception {
    myFixture.addFileToProject("file1.dart", "var inFile1;");
    myFixture.addFileToProject("file2.dart", "var inFile2;");
    doTest("import 'dart:core' as prefix;\n" +
           "import 'file1.dart' as prefix;\n" +
           "import 'file2.dart' as prefix;\n" +
           "var a = prefix.int<caret expected='[Dart SDK]/lib/core/int.dart -> int'>;\n" +
           "var b = prefix.inFile1<caret expected='file1.dart -> inFile1'>;\n" +
           "var c = prefix.inFile2<caret expected='file2.dart -> inFile2'>;");
  }
}
