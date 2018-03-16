// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.jetbrains.dart.analysisServer;

import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.file.PsiDirectoryImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.util.CaretPositionInfo;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DartServerResolverTest extends CodeInsightFixtureTestCase {
  @Override
  public void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule, myFixture.getTestRootDisposable(), true);
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
  }

  public static void doTest(@NotNull final CodeInsightTestFixture fixture, @NotNull final String text) {
    PsiFile file = fixture.getFile();
    if (file == null) {
      file = fixture.addFileToProject("file.dart", text);
      fixture.openFileInEditor(file.getVirtualFile());
    }
    else {
      final Document document = FileDocumentManager.getInstance().getDocument(file.getVirtualFile());
      assert document != null;
      ApplicationManager.getApplication().runWriteAction(() -> document.setText(text));
    }

    doTest(fixture);
  }

  public static void doTest(@NotNull final CodeInsightTestFixture fixture) {
    final List<CaretPositionInfo> caretPositions =
      DartTestUtils.extractPositionMarkers(fixture.getProject(), fixture.getEditor().getDocument());

    fixture.doHighlighting();

    for (CaretPositionInfo caretPositionInfo : caretPositions) {
      final int line = fixture.getEditor().getDocument().getLineNumber(caretPositionInfo.caretOffset);
      final int column = caretPositionInfo.caretOffset - fixture.getEditor().getDocument().getLineStartOffset(line);
      final String fileNameAndPosition = fixture.getFile().getName() + ":" + (line + 1) + ":" + (column + 1);

      final PsiReference reference = TargetElementUtil.findReference(fixture.getEditor(), caretPositionInfo.caretOffset);
      assertNotNull("No reference in " + fileNameAndPosition, reference);

      final PsiElement resolve = reference.resolve();
      final String actualElementPosition = getPresentableElementPosition(fixture, resolve);
      assertEquals("Incorrect resolution for reference in " + fileNameAndPosition, caretPositionInfo.expected, actualElementPosition);
    }
  }

  @NotNull
  private static String getPresentableElementPosition(@NotNull final CodeInsightTestFixture fixture, final @Nullable PsiElement element) {
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

    final String contentRoot = ModuleRootManager.getInstance(fixture.getModule()).getContentRoots()[0].getPath();
    if (path.equals(contentRoot)) path = "[content root]";

    final String contentRootWithSlash = contentRoot + "/";
    path = StringUtil.trimStart(path, contentRootWithSlash);

    final DartSdk sdk = DartSdk.getDartSdk(element.getProject());
    if (sdk != null && path.startsWith(sdk.getHomePath())) path = "[Dart SDK]" + path.substring(sdk.getHomePath().length());

    if (buf.length() > 0) buf.insert(0, " -> ");
    buf.insert(0, path);

    return buf.toString();
  }

  public void testNoRecursiveImports() {
    myFixture.addFileToProject("file2.dart", "inFile2(){}");
    myFixture.addFileToProject("file1.dart", "import 'file2.dart'\n inFile1(){}");
    myFixture.addFileToProject("file.dart", "library fileLib;\n" +
                                            "import 'file1.dart';\n" +
                                            "part 'filePart1.dart';\n" +
                                            "part 'filePart2.dart';\n" +
                                            "inFile(){}");
    myFixture.addFileToProject("filePart1.dart", "part of fileLib;\n" +
                                                 "inFilePart1(){}");
    final PsiFile file = myFixture.addFileToProject("filePart2.dart", "part of fileLib;\n" +
                                                                      "inFilePart2(){\n" +
                                                                      "  <caret expected='filePart1.dart -> inFilePart1'>inFilePart1()\n" +
                                                                      "  <caret expected='filePart2.dart -> inFilePart2'>inFilePart2()\n" +
                                                                      "  <caret expected='file.dart -> inFile'>inFile()\n" +
                                                                      "  <caret expected='file1.dart -> inFile1'>inFile1()\n" +
                                                                      "  <caret expected=''>inFile2()\n" +
                                                                      "}");
    myFixture.openFileInEditor(file.getVirtualFile());
    doTest(myFixture);
  }

  public void testResolveWithExports() {
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
    final PsiFile file = myFixture.addFileToProject("filePart1.dart", "part of fileLib;\n" +
                                                                      "foo(){\n" +
                                                                      "  <caret expected='file.dart -> inFile'>inFile()\n" +
                                                                      // IDE's resolver is buggy and doesn't resolve inFile8() руку
                                                                      "  <caret expected='file8.dart -> inFile8'>inFile8()\n" +
                                                                      "  <caret expected='file7.dart -> inFile7'>inFile7()\n" +
                                                                      "  <caret expected='file6.dart -> inFile6'>inFile6()\n" +
                                                                      "  <caret expected='file5.dart -> inFile5'>inFile5()\n" +
                                                                      "  <caret expected=''>inFile4()\n" +
                                                                      "  <caret expected=''>inFile3()\n" +
                                                                      "  <caret expected='file2.dart -> inFile2'>inFile2()\n" +
                                                                      "  <caret expected='file1.dart -> inFile1'>inFile1()\n" +
                                                                      "  <caret expected=''>inFile1HiddenLater()\n" +
                                                                      "}");
    myFixture.openFileInEditor(file.getVirtualFile());
    doTest(myFixture);
  }

  public void testFileReferencesInImports() {
    myFixture.addFileToProject("pubspec.yaml", "name: ProjectName\n" +
                                               "dependencies:\n" +
                                               "  PathPackage:\n" +
                                               "    path: local_package\n");
    myFixture.addFileToProject(".packages", "ProjectName:lib/\n" +
                                            "PathPackage:local_package/lib/\n" +
                                            "SomePackage:packages/SomePackage");
    myFixture.addFileToProject("local_package/lib/localPackageLib1.dart", "");
    myFixture.addFileToProject("local_package/lib/src/localPackageLib2.dart", "");
    myFixture.addFileToProject("packages/ProjectName/src/file9.dart", ""); // symlink to lib/src/file9.dart
    myFixture.addFileToProject("packages/ProjectName/file1.dart", ""); // symlink to lib/file1.dart
    myFixture.addFileToProject("packages/ProjectName/file2.dart", ""); // symlink to lib/file2.dart
    myFixture.addFileToProject("packages/PathPackage/localPackageLib1.dart", ""); // symlink to local_package/lib/localPackageLib1.dart
    myFixture
      .addFileToProject("packages/PathPackage/src/localPackageLib2.dart", ""); // symlink to local_package/lib/src/localPackageLib2.dart
    myFixture.addFileToProject("packages/SomePackage/somePack1.dart", "");
    myFixture.addFileToProject("packages/SomePackage/src/somePack2.dart", "");
    myFixture.addFileToProject("lib/src/file9.dart", "");
    myFixture.addFileToProject("lib/file1.dart", "");

    // for IDE-based resolution (the whole URI is a single reference in this case)
    //final PsiFile psiFile = myFixture.addFileToProject(
    //  "lib/file2.dart",
    //
    //  "import '<caret expected='lib/file1.dart'>file1.dart<caret expected='lib/file1.dart'>';\n" +
    //  "import '<caret expected='lib'>.<caret expected='lib'>/<caret expected='lib/file1.dart'>file1.dart<caret expected='lib/file1.dart'>';\n" +
    //  "import '<caret expected='[content root]'>..<caret expected='[content root]'>/<caret expected='lib'>lib<caret expected='lib'>/<caret expected='lib/file1.dart'>file1.dart<caret expected='lib/file1.dart'>';\n" +
    //  "import '<caret expected='lib/src'>src<caret expected='lib/src'>/<caret expected='lib/src/file9.dart'>file9.dart<caret expected='lib/src/file9.dart'>';\n" +
    // todo not sure that DartStringLiteralExpression should be here as reference
    //"import 'package:ProjectName<caret expected=''>/<caret expected='lib/src'>src<caret expected='lib/src'>/<caret expected='lib/src/file9.dart'>file9.dart<caret expected='lib/src/file9.dart'>';\n" +
    //"import 'package:ProjectName<caret expected=''>/<caret expected='lib/file1.dart'>file1.dart<caret expected='lib/file1.dart'>';\n" +
    //"import 'package:PathPackage<caret expected=''>/<caret expected='local_package/lib/localPackageLib1.dart'>localPackageLib1.dart<caret expected='local_package/lib/localPackageLib1.dart'>';\n" +
    //"import 'package:PathPackage<caret expected=''>/<caret expected='local_package/lib/src'>src<caret expected='local_package/lib/src'>/<caret expected='local_package/lib/src/localPackageLib2.dart'>localPackageLib2.dart<caret expected='local_package/lib/src/localPackageLib2.dart'>';\n" +
    //"import 'package<caret expected=''>:SomePackage/<caret expected='packages/SomePackage/somePack1.dart'>somePack1.dart<caret expected='packages/SomePackage/somePack1.dart'>';\n" +
    //"import 'package<caret expected=''>:SomePackage/<caret expected='packages/SomePackage/src'>src<caret expected='packages/SomePackage/src'>/<caret expected='packages/SomePackage/src/somePack2.dart'>somePack2.dart<caret expected='packages/SomePackage/src/somePack2.dart'>';\n" +
    //""
    //);

    // for server-based resolution (the whole URI is a single reference in this case)
    final PsiFile psiFile = myFixture.addFileToProject(
      "lib/file2.dart",

      "import '<caret expected='lib/file1.dart'>file1.dart<caret expected='lib/file1.dart'>';\n" +
      "import '.<caret expected='lib/file1.dart'>/<caret expected='lib/file1.dart'>file1.dart<caret expected='lib/file1.dart'>';\n" +
      //"import '<caret expected='lib/file1.dart'>..<caret expected='lib/file1.dart'>/<caret expected='lib/file1.dart'>lib<caret expected='lib/file1.dart'>/<caret expected='lib/file1.dart'>file1.dart<caret expected='lib/file1.dart'>';\n" +
      "import '<caret expected='lib/src/file9.dart'>src<caret expected='lib/src/file9.dart'>/<caret expected='lib/src/file9.dart'>file9.dart<caret expected='lib/src/file9.dart'>';\n" +
      // todo not sure that DartStringLiteralExpression should be here as reference
      "import 'package:ProjectName<caret expected='lib/src/file9.dart'>/<caret expected='lib/src/file9.dart'>src<caret expected='lib/src/file9.dart'>/<caret expected='lib/src/file9.dart'>file9.dart<caret expected='lib/src/file9.dart'>';\n" +
      "import 'package:ProjectName<caret expected='lib/file1.dart'>/<caret expected='lib/file1.dart'>file1.dart<caret expected='lib/file1.dart'>';\n" +
      "import 'package:PathPackage<caret expected='local_package/lib/localPackageLib1.dart'>/<caret expected='local_package/lib/localPackageLib1.dart'>localPackageLib1.dart<caret expected='local_package/lib/localPackageLib1.dart'>';\n" +
      "import 'package:PathPackage<caret expected='local_package/lib/src/localPackageLib2.dart'>/<caret expected='local_package/lib/src/localPackageLib2.dart'>src<caret expected='local_package/lib/src/localPackageLib2.dart'>/<caret expected='local_package/lib/src/localPackageLib2.dart'>localPackageLib2.dart<caret expected='local_package/lib/src/localPackageLib2.dart'>';\n" +
      "import 'package<caret expected='packages/SomePackage/somePack1.dart'>:SomePackage/<caret expected='packages/SomePackage/somePack1.dart'>somePack1.dart<caret expected='packages/SomePackage/somePack1.dart'>';\n" +
      "import 'package<caret expected='packages/SomePackage/src/somePack2.dart'>:SomePackage/<caret expected='packages/SomePackage/src/somePack2.dart'>src<caret expected='packages/SomePackage/src/somePack2.dart'>/<caret expected='packages/SomePackage/src/somePack2.dart'>somePack2.dart<caret expected='packages/SomePackage/src/somePack2.dart'>';\n" +
      ""
    );
    myFixture.openFileInEditor(psiFile.getVirtualFile());

    doTest(myFixture);
  }

  public void testShowHideInImports() {
    myFixture.addFileToProject("file1.dart", "foo1(){}\n" +
                                             "foo2(){}\n" +
                                             "foo3(){}\n" +
                                             "foo4(){}\n");
    doTest(myFixture,
           "import 'file1.dart' show foo1;\n" +
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

  public void testTransitiveShowHide() {
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
    final PsiFile file = myFixture.addFileToProject("filepart.dart", "part of filelib;\n" +
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
    myFixture.openFileInEditor(file.getVirtualFile());
    doTest(myFixture);
  }

  public void testMixinApplication() {
    doTest(myFixture,
           "class Super  { inSuper(){}   }\n" +
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

  public void testExceptionParameters() {
    doTest(myFixture,
           "main(){\n" +
           "  try{} on Error catch (e1){<caret expected='file.dart -> main -> e1'>e1;}\n" +
           "  try{} on Error catch (e2, s2){<caret expected='file.dart -> main -> e2'>e2 + <caret expected='file.dart -> main -> s2'>s2; <caret expected=''>e1;}\n" +
           "  try{} catch (e3){<caret expected='file.dart -> main -> e3'>e3; <caret expected=''>e1; <caret expected=''>e2; <caret expected=''>s2;}\n" +
           "  try{} catch (e4, s4){print(<caret expected='file.dart -> main -> e4'>e4 + <caret expected='file.dart -> main -> s4'>s4); <caret expected=''>e1; <caret expected=''>e2; <caret expected=''>s2; <caret expected=''>e3;}\n" +
           "}");
  }

  public void testObjectMembers() {
    doTest(myFixture,
           "class Bar{}\n" +
           "class Foo extends Bar{\n" +
           "  String<caret expected='[Dart SDK]/lib/core/string.dart -> String'> toString(){\n" +
           "    var i = hashCode<caret expected='[Dart SDK]/lib/core/object.dart -> Object -> hashCode'>;\n" +
           "    var b = new Bar();\n" +
           "    b.runtimeType<caret expected='[Dart SDK]/lib/core/object.dart -> Object -> runtimeType'>;\n" +
           "    return super.toString<caret expected='[Dart SDK]/lib/core/object.dart -> Object -> toString'>();\n" +
           "  }\n" +
           "}");
  }

  public void testCommentsInsideCallExpression() {
    doTest(myFixture,
           "main(){\n" +
           "  '1 2 3 4'.<caret expected='[Dart SDK]/lib/core/string.dart -> String -> split'>split(' ')  // comment\n" +
           "  .map<caret expected='[Dart SDK]/lib/core/iterable.dart -> Iterable -> map'>((e) => int.<caret expected='[Dart SDK]/lib/core/int.dart -> int -> parse'>parse(e) * 2);\n" +
           "}");
  }

  public void testEnum() {
    doTest(myFixture,
           "enum Foo {FooA, FooB, }\n" +
           "main() {\n" +
           "  print(<caret expected='file.dart -> Foo'>Foo.<caret expected='file.dart -> Foo -> FooB'>FooB.<caret expected='[Dart SDK]/lib/core/object.dart -> Object -> toString'>toString());\n" +
           "}");
  }

  public void testPartViaPackageUrl() {
    myFixture.addFileToProject("pubspec.yaml", "name: ProjectName\n");
    myFixture.addFileToProject(".packages", "ProjectName:lib/");
    myFixture.addFileToProject("lib/lib.dart", "part 'package:ProjectName/part.dart';");
    myFixture.addFileToProject("lib/part.dart", "var foo;");
    doTest(myFixture,
           "import 'package:ProjectName/lib.dart';'\n" +
           "main() {\n" +
           "  var a = <caret expected='lib/part.dart -> foo'>foo;\n" +
           "}");
  }

  public void testNamedParameter_constructorDefaultInvocation() {
    doTest(myFixture,
           "class A {\n" +
           "  A({test});\n" +
           "}\n" +
           "main() {\n" +
           "  new A(te<caret expected='file.dart -> A -> A -> test'>st: 0);\n" +
           "}");
  }

  public void testNamedParameter_constructorNamedInvocation() {
    doTest(myFixture,
           "class A {\n" +
           "  A.named({test});\n" +
           "}\n" +
           "main() {\n" +
           "  new A.named(te<caret expected='file.dart -> A -> named -> test'>st: 0);\n" +
           "}");
  }

  public void testNamedParameter_functionInvocation() {
    doTest(myFixture,
           "f({test}) {}\n" +
           "main() {\n" +
           "  f(te<caret expected='file.dart -> f -> test'>st: 0);\n" +
           "}");
  }

  public void testFromPartToPartViaPackageUrl() {
    myFixture.addFileToProject("pubspec.yaml", "name: ProjectName\n");
    myFixture.addFileToProject(".packages", "ProjectName:lib/\n");
    myFixture.addFileToProject("lib/lib.dart", "library libName;\n" +
                                               "part 'package:ProjectName/part1.dart';\n" +
                                               "part 'package:ProjectName/part2.dart';");
    myFixture.addFileToProject("lib/part1.dart", "part of libName;" +
                                                 "var foo1;");
    final PsiFile psiFile = myFixture.addFileToProject("lib/part2.dart", "part of libName;\n" +
                                                                         "var foo2 = <caret expected='lib/part1.dart -> foo1'>foo1;");
    myFixture.openFileInEditor(psiFile.getVirtualFile());
    doTest(myFixture);
  }

  public void testDartInternalLibrary() {
    doTest(myFixture,
           "import 'dart:_internal';\n" +
           "class A extends <caret expected='[Dart SDK]/lib/internal/list.dart -> UnmodifiableListBase'>UnmodifiableListBase<E>{}"
    );
  }

  public void testPartOfResolution() {
    myFixture.addFileToProject("main1.dart", "library lib.name;\npart 'part1.dart';");
    myFixture.addFileToProject("main2.dart", "library lib.name;\npart 'part2.dart';");
    final PsiFile file = myFixture.addFileToProject("part1.dart",
                                                    "  part of <caret expected='main1.dart -> lib.name'>lib.name<caret expected='main1.dart -> lib.name'> ;");
    myFixture.openFileInEditor(file.getVirtualFile());
    doTest(myFixture);
  }

  public void testDuplicatedImportPrefix() {
    myFixture.addFileToProject("file1.dart", "var inFile1;");
    myFixture.addFileToProject("file2.dart", "var inFile2;");
    doTest(myFixture,
           "import 'dart:core' as prefix;\n" +
           "import 'file1.dart' as prefix;\n" +
           "import 'file2.dart' as prefix;\n" +
           "var a = prefix.int<caret expected='[Dart SDK]/lib/core/int.dart -> int'>;\n" +
           "var b = prefix.inFile1<caret expected='file1.dart -> inFile1'>;\n" +
           "var c = prefix.inFile2<caret expected='file2.dart -> inFile2'>;");
  }

  public void testCoreLibImported() {
    doTest(myFixture, "var a = String<caret expected='[Dart SDK]/lib/core/string.dart -> String'>;");
    doTest(myFixture, "import 'dart:core'; var a = String<caret expected='[Dart SDK]/lib/core/string.dart -> String'>;");
    doTest(myFixture, "import 'dart:core' as prefix; var a = String<caret expected=''>;");
    doTest(myFixture, "import 'dart:core' show int;\n" +
                      "var a = String<caret expected=''>, b = int<caret expected='[Dart SDK]/lib/core/int.dart -> int'>;");
    doTest(myFixture, "import 'dart:core' hide int;\n" +
                      "var a = String<caret expected='[Dart SDK]/lib/core/string.dart -> String'>, b = int<caret expected=''>;");
    doTest(myFixture,
           "import 'dart:core' as prefix; var a = prefix.String<caret expected='[Dart SDK]/lib/core/string.dart -> String'>;");
    doTest(myFixture, "import 'dart:core' as prefix show int;\n" +
                      "var a = prefix.String<caret expected=''>, b = prefix.int<caret expected='[Dart SDK]/lib/core/int.dart -> int'>;");
    doTest(myFixture, "import 'dart:core' deferred as prefix hide int;\n" +
                      "var a = prefix.String<caret expected='[Dart SDK]/lib/core/string.dart -> String'>, b = prefix.int<caret expected=''>;"
    );

    myFixture.addFileToProject("file1.dart", "var inFile1;");
    doTest(myFixture,
           "import 'file1.dart' as prefix;\n" +
           "var a = prefix.String<caret expected=''>;\n" +
           "var b = prefix.inFile1<caret expected='file1.dart -> inFile1'>;\n" +
           "var c = String<caret expected='[Dart SDK]/lib/core/string.dart -> String'>;");

    myFixture.addFileToProject("file2.dart", "export 'dart:core' show Object; var inFile2;");
    doTest(myFixture,
           "import 'file2.dart' as prefix;\n" +
           "var a = prefix.String<caret expected=''>;\n" +
           "var b = prefix.Object<caret expected='[Dart SDK]/lib/core/object.dart -> Object'>;\n" +
           "var c = prefix.inFile2<caret expected='file2.dart -> inFile2'>;\n" +
           "var d = Object<caret expected='[Dart SDK]/lib/core/object.dart -> Object'>;");
  }

  public void testTransitivePathPackageDependencies() {
    myFixture.addFileToProject("project1/pubspec.yaml", "name: project1\n" +
                                                        "dependencies:\n" +
                                                        "  project2:\n" +
                                                        "    path: ../project2\n");
    myFixture.addFileToProject("project1/.packages", "project1:lib/\n" +
                                                     "project2:../project2/lib/\n" +
                                                     "project3:../project3/lib/\n");
    myFixture.addFileToProject("project2/pubspec.yaml", "name: project2\n" +
                                                        "dependencies:\n" +
                                                        "  project1:\n" +
                                                        "    path: ../project1\n" +
                                                        "  project3:\n" +
                                                        "    path: ../project3\n");
    myFixture.addFileToProject("project3/pubspec.yaml", "name: project3\n");
    myFixture.addFileToProject("project3/.packages", "project3:lib/\n");

    myFixture.addFileToProject("project2/lib/in_lib2.dart", "inLib2(){}");
    myFixture.addFileToProject("project3/lib/in_lib3.dart", "inLib3(){}");

    final PsiFile psiFile = myFixture.addFileToProject("project1/lib/foo.dart",
                                                       "import 'package:project2/in_lib2.dart';\n" +
                                                       "import 'package:project3/in_lib3.dart';\n" +
                                                       "main(){\n" +
                                                       "  inLib2<caret expected='project2/lib/in_lib2.dart -> inLib2'>();\n" +
                                                       "  inLib3<caret expected='project3/lib/in_lib3.dart -> inLib3'>();\n" +
                                                       "}");
    myFixture.openFileInEditor(psiFile.getVirtualFile());
    doTest(myFixture);
  }

  public void testElvisRes() {
    doTest(myFixture,
           "class Bar{}\n" +
           "class Foo extends Bar{\n" +
           "  voor(){\n" +
           "    var i = this?.hashCode<caret expected='[Dart SDK]/lib/core/object.dart -> Object -> hashCode'>;\n" +
           "    var b = new Bar();\n" +
           "    b?.runtimeType<caret expected='[Dart SDK]/lib/core/object.dart -> Object -> runtimeType'>;\n" +
           "  }\n" +
           "}");
  }

  public void testConstructors() {
    doTest(myFixture,
           "var a = new <caret expected='file.dart -> Foo -> Foo'>Foo<caret expected='file.dart -> Foo -> Foo'>();\n" +
           "var b = new <caret expected='file.dart -> Foo -> named'>Foo<caret expected='file.dart -> Foo -> named'>.<caret expected='file.dart -> Foo -> named'>named<caret expected='file.dart -> Foo -> named'>();\n" +
           "var c = new <caret expected='file.dart -> Bar -> Bar'>Bar<caret expected='file.dart -> Bar -> Bar'>();\n" +
           "var d = new <caret expected='file.dart -> Bar -> named'>Bar<caret expected='file.dart -> Bar -> named'>.<caret expected='file.dart -> Bar -> named'>named<caret expected='file.dart -> Bar -> named'>();\n" +
           "class Foo {\n" +
           "  Foo(){}\n" +
           "  Foo.named() {}\n" +
           "}\nclass Bar {\n" +
           "  factory Bar() {}\n" +
           "  factory Bar.named() {}\n" +
           "}");
  }

  public void testRedirectingConstructorInvocation() {
    doTest(myFixture,
           "class A {\n" +
           "  A() {}\n" +
           "  A.foo() : <caret expected='file.dart -> A -> A'>this();\n" +
           "  A.bar() : <caret expected='file.dart -> A -> foo'>this.<caret expected='file.dart -> A -> foo'>foo();\n" +
           "}");
  }

  public void testSuperConstructorInvocation() {
    doTest(myFixture,
           "class A {\n" +
           "  A() {}\n" +
           "  A.foo() {}\n" +
           "}\n" +
           "class B extends A {\n" +
           "  B() : <caret expected='file.dart -> A -> A'>super();\n" +
           "  B.foo() : <caret expected='file.dart -> A -> foo'>super.<caret expected='file.dart -> A -> foo'>foo();\n" +
           "}");
  }

  public void testRefsInDocComments() {
    doTest(myFixture,
           "/// [<caret expected='[Dart SDK]/lib/core/object.dart -> Object'>Object] foo\n" +
           "/// [print<caret expected='[Dart SDK]/lib/core/print.dart -> print'>\n" +
           "/// [ <caret expected='file.dart -> a'>a ]\n" +
           "var a;\n" +
           "/**\n" +
           " * [Object<caret expected='[Dart SDK]/lib/core/object.dart -> Object'>.<caret expected='[Dart SDK]/lib/core/object.dart -> Object -> =='>==]\n" +
           " */\n" +
           "var b;");
  }
}
