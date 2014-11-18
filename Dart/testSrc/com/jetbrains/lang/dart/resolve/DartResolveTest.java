package com.jetbrains.lang.dart.resolve;

import com.intellij.codeInsight.TargetElementUtilBase;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.file.PsiDirectoryImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.hash.LinkedHashMap;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class DartResolveTest extends DartCodeInsightFixtureTestCase {

  private void doTest() {
    final LinkedHashMap<Integer, String> caretOffsetToExpectedResult =
      DartTestUtils.extractPositionMarkers(getProject(), myFixture.getEditor().getDocument());

    for (Map.Entry<Integer, String> entry : caretOffsetToExpectedResult.entrySet()) {
      final Integer caretOffset = entry.getKey();
      final String expectedResult = entry.getValue();

      final int line = myFixture.getEditor().getDocument().getLineNumber(caretOffset);
      final int column = caretOffset - myFixture.getEditor().getDocument().getLineStartOffset(line);
      final String fileNameAndPosition = myFixture.getFile().getName() + ":" + (line + 1) + ":" + (column + 1);

      final PsiReference reference = TargetElementUtilBase.findReference(myFixture.getEditor(), caretOffset);
      assertNotNull("No reference in " + fileNameAndPosition, reference);

      final PsiElement resolve = reference.resolve();
      final String actualElementPosition = getPresentableElementPosition(resolve);
      assertEquals("Incorrect resolve for element in " + fileNameAndPosition, expectedResult, actualElementPosition);
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
    myFixture.configureByText("file.dart", "import 'file1.dart' show foo1;\n" +
                                           "import 'file1.dart' show foo2;\n" +
                                           "import 'file1.dart' hide foo1, foo2, foo3, foo4;\n" +
                                           "import 'file1.dart' show foo3;\n" +
                                           "main(){\n" +
                                           "  <caret expected='file1.dart -> foo1'>foo1();\n" +
                                           "  <caret expected='file1.dart -> foo2'>foo2();\n" +
                                           "  <caret expected='file1.dart -> foo3'>foo3();\n" +
                                           "  <caret expected=''>foo4();\n" +
                                           "}");
    doTest();
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
    myFixture.configureByText("file.dart", "class Super  { inSuper(){}   }\n" +
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
    doTest();
  }

  public void testExceptionParameters() throws Exception {
    myFixture.configureByText("file.dart", "main(){\n" +
                                           "  try{} on Error catch (e1){<caret expected='file.dart -> main -> e1'>e1;}\n" +
                                           "  try{} on Error catch (e2, s2){<caret expected='file.dart -> main -> e2'>e2 + <caret expected='file.dart -> main -> s2'>s2; <caret expected=''>e1;}\n" +
                                           "  try{} catch (e3){<caret expected='file.dart -> main -> e3'>e3; <caret expected=''>e1; <caret expected=''>e2; <caret expected=''>s2;}\n" +
                                           "  try{} catch (e4, s4){print(<caret expected='file.dart -> main -> e4'>e4 + <caret expected='file.dart -> main -> s4'>s4); <caret expected=''>e1; <caret expected=''>e2; <caret expected=''>s2; <caret expected=''>e3;}\n" +
                                           "}");
    doTest();
  }

  public void testObjectMembers() throws Exception {
    myFixture.configureByText("file.dart", "class Bar{}\n" +
                                           "class Foo extends Bar{\n" +
                                           "  String toString(){\n" +
                                           "    var i = hashCode<caret expected='[Dart SDK]/lib/core/object.dart -> Object -> hashCode'>;\n" +
                                           "    var b = new Bar();\n" +
                                           "    b.runtimeType<caret expected='[Dart SDK]/lib/core/object.dart -> Object -> runtimeType'>;\n" +
                                           "    return super.toString<caret expected='[Dart SDK]/lib/core/object.dart -> Object -> toString'>();\n" +
                                           "  }\n" +
                                           "}");
    doTest();
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
    myFixture.configureByText("file.dart", "main(){\n" +
                                           "  '1 2 3 4'.<caret expected='[Dart SDK]/lib/core/string.dart -> String -> split'>split(' ')  // comment\n" +
                                           "  .map<caret expected='[Dart SDK]/lib/core/iterable.dart -> Iterable -> map'>((e) => int.<caret expected='[Dart SDK]/lib/core/int.dart -> int -> parse'>parse(e) * 2);\n" +
                                           "}");
    doTest();
  }

  public void testEnum() throws Exception {
    myFixture.configureByText("file.dart", "enum Foo {FooA, FooB, }\n" +
                                           "main() {\n" +
                                           "  print(<caret expected='file.dart -> Foo'>Foo.<caret expected='file.dart -> Foo -> FooB'>FooB);\n" +
                                           "}");
    doTest();
  }

  public void testPartViaPackageUrl() throws Exception {
    myFixture.addFileToProject("pubspec.yaml", "name: ProjectName\n");
    myFixture.addFileToProject("lib/lib.dart", "part 'package:ProjectName/part.dart';");
    myFixture.addFileToProject("lib/part.dart", "var foo;");
    myFixture.configureByText("file.dart", "import 'package:ProjectName/lib.dart';'\n" +
                                           "main() {\n" +
                                           "  var a = <caret expected='lib/part.dart -> foo'>foo;\n" +
                                           "}");
    doTest();
  }
}
