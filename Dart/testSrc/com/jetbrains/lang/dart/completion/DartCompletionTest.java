package com.jetbrains.lang.dart.completion;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.util.CaretPositionInfo;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DartCompletionTest extends DartCodeInsightFixtureTestCase {

  @Override
  protected String getBasePath() {
    return "/completion";
  }

  private void doTest(@NotNull final String text) {
    doTest("web/" + getTestName(true) + ".dart", text);
  }

  private void doTest(@NotNull final String fileRelPath, final String text) {
    final PsiFile file = myFixture.addFileToProject(fileRelPath, text);
    myFixture.openFileInEditor(file.getVirtualFile());

    final List<CaretPositionInfo> caretPositions = DartTestUtils.extractPositionMarkers(getProject(), myFixture.getEditor().getDocument());
    final String fileText = file.getText();

    for (CaretPositionInfo caretPositionInfo : caretPositions) {
      myFixture.getEditor().getCaretModel().moveToOffset(caretPositionInfo.caretOffset);
      final LookupElement[] lookupElements = myFixture.completeBasic();
      final String fileTextWithCaret =
        fileText.substring(0, caretPositionInfo.caretOffset) + "<caret>" + fileText.substring(caretPositionInfo.caretOffset);
      checkLookupElements(lookupElements,
                          caretPositionInfo.completionEqualsList,
                          caretPositionInfo.completionIncludesList,
                          caretPositionInfo.completionExcludesList,
                          fileTextWithCaret);
      LookupManager.getInstance(getProject()).hideActiveLookup();
    }
  }

  private static void checkLookupElements(@NotNull final LookupElement[] lookupElements,
                                          @Nullable final List<String> equalsList,
                                          @Nullable final List<String> includesList,
                                          @Nullable final List<String> excludesList,
                                          @NotNull final String fileTextWithCaret) {
    final List<String> lookupStrings = new ArrayList<String>();
    for (LookupElement element : lookupElements) {
      lookupStrings.add(element.getLookupString());
    }

    if (equalsList != null) {
      assertSameElements(fileTextWithCaret, lookupStrings, equalsList);
    }

    if (includesList != null) {
      for (String s : includesList) {
        if (!lookupStrings.contains(s)) {
          fail("Missing [" + s + "] in completion list:\n" + StringUtil.join(lookupStrings, "\n") +
               "\n### Caret position:\n" + fileTextWithCaret);
        }
      }
    }

    if (excludesList != null) {
      for (String s : excludesList) {
        if (lookupStrings.contains(s)) {
          fail("Unexpected variant [" + s + "] in completion list:\n" + StringUtil.join(lookupStrings, "\n") +
               "\n### Caret position:\n" + fileTextWithCaret);
        }
      }
    }
  }

  public void testUriBasedDirectives() throws Exception {
    final String sdkLibs =
      "dart:async,dart:collection,dart:convert,dart:core,dart:html,dart:html_common,dart:indexed_db,dart:io,dart:isolate,dart:js," +
      "dart:math,dart:mirrors,dart:nativewrappers,dart:profiler,dart:svg,dart:typed_data,dart:web_audio,dart:web_gl,dart:web_sql";
    addStandardPackage("polymer");
    addStandardPackage("core_elements");
    myFixture.addFileToProject("pubspec.yaml", "");
    myFixture.addFileToProject("web/other1.dart", "");
    myFixture.addFileToProject("web/foo/other2.dart", "");
    myFixture.addFileToProject("web/other3.xml", "");
    doTest("import '''<caret completionEquals='" + sdkLibs + ",foo,other1.dart,package:'>''';\n" +
           "export r\"<caret completionEquals='" + sdkLibs + ",foo,other1.dart,package:'>\n" +
           "part '<caret completionEquals='foo,other1.dart,package:'>'\n" +
           "import 'dart:<caret completionEquals='" + sdkLibs + "'>';\n " +
           "import 'foo/<caret completionEquals='other2.dart'>z';\n" +
           "import 'package:<caret completionEquals='polymer,core_elements'>';\n" +
           "import 'package:polymer/<caret completionEquals='src,polymer.dart,transformer.dart'>");
  }

  public void testPackageFolderCompletionInHtml() throws Throwable {
    myFixture.addFileToProject("pubspec.yaml", "");
    myFixture.addFileToProject("packages/browser/dart.js", "");
    myFixture.addFileToProject("web/other.dart", "");
    doTest("web/file.html", "<link href='file.html,other.dart,packages'>");
  }

  public void testLivePackageNameCompletionInHtml() throws Throwable {
    myFixture.addFileToProject("pubspec.yaml", "name: ProjectName\n" +
                                               "dependencies:\n" +
                                               "  PathPackage:\n" +
                                               "    path: local_package\n");
    myFixture.addFileToProject("lib/projectFile.dart", "");
    myFixture.addFileToProject("local_package/lib/localPackageFile.html", "");
    myFixture.addFileToProject("packages/browser/dart.js", "");
    doTest("web/file.html", "<link href='packages/<caret completionEquals='ProjectName,PathPackage,browser'>");
  }

  public void testLivePackageContentCompletionInHtml() throws Throwable {
    myFixture.addFileToProject("pubspec.yaml", "name: ProjectName\n");
    myFixture.addFileToProject("lib/projectFile.dart", "");
    doTest("web/file.html", "<link href='packages/ProjectName/<caret expectedEquals='projectFile.dart>x'>");
  }

  public void testKeywords() throws Exception {
    doTest("Object <caret completionEquals=''>a;\n" +
           "var <caret completionEquals=''>a;\n" +
           "void function (Object O<caret completionEquals=''>){}\n");
  }

  public void testNoDuplication() throws Exception {
    myFixture.addFileToProject("web/part.dart", "part of lib; class FooBaz{}");
    doTest("library lib; part 'part.dart'; class FooBar{ const Foob<caret completionEquals='FooBar,FooBaz'> }");
  }

  public void testClassNameCompletion() throws Exception {
    myFixture.addFileToProject("pubspec.yaml", "name: ProjectName\n");
    myFixture.addFileToProject("lib/in_lib.dart", "library lib; export 'exported.dart'; part 'lib_part.dart'; class InLib{}");
    myFixture.addFileToProject("lib/lib_part.dart", "part of lib; class InLibPart{} class InLibPartHidden{} class _InLibPartPrivate{}");
    myFixture.addFileToProject("lib/exported.dart", "class InExported{} class _InExported{}");
    myFixture.addFileToProject("lib/in_lib2.dart", "class InLib2{}\n" +
                                                   "class _InLib2{}\n" +
                                                   "enum Enum {enumConstant}\n" +
                                                   "typedef void Typedef(param);");
    myFixture.addFileToProject("tool/in_tool.dart", "class InTool{} class _InTool{}");
    myFixture.addFileToProject("packages/SomePackage/in_package1.dart",
                               "class InPackage1{} class InPackage1NotShown{} class _InPackage1{}");
    myFixture.addFileToProject("packages/SomePackage/in_package2.dart", "class InPackage2{} class _InPackage2{}");
    myFixture.addFileToProject("web/web.dart", "library web;\n" +
                                               "import 'package:ProjectName/in_lib.dart' hide InLibPartHidden;\n" +
                                               "import 'package:SomePackage/in_package1.dart' show InPackage1;\n" +
                                               "part 'web_part.dart';\n" +
                                               "part '" + getTestName(true) + ".dart';\n" +
                                               "class InWeb{}\n" +
                                               "class _InWeb{}");
    myFixture.addFileToProject("web/web_part.dart", "part of web; class InWebPart{} class _InWebPart{}");
    doTest("part of web;\n" +
           "class InWebPart2{}\n" +
           "class _InWebPart2{}\n" +
           "const <caret" +
           " completionIncludes='Object,String,int,bool,Iterable,Set,StateError,InLib,InLibPart,InExported,InPackage1,InWeb,_InWeb,InWebPart,_InWebPart,InWebPart2,_InWebPart2'" +
           " completionExcludes='InLibPartHidden,_InLibPartPrivate,_InExported,_InLib2,_InTool,_InPackage1,_InPackage2,InPackage1NotShown,InLib2,Enum,Typedef,InTool,InPackage2,SetMixin,FixedLengthListMixin,Point,JsObject,_Proxy,_SplayTree'>");

    final LookupElement[] lookupElements = myFixture.complete(CompletionType.BASIC, 2);
    final List<String> includes =
      Arrays.asList("Object", "String", "int", "bool", "Iterable", "Set", "StateError", "InLib", "InLibPart", "InExported", "InPackage1",
                    "InWeb", "_InWeb", "InWebPart", "_InWebPart", "InWebPart2", "_InWebPart2", "InLibPartHidden", "InPackage1NotShown",
                    "InLib2", "Enum", "Typedef", "InPackage2", "SetMixin", "Point", "JsObject");
    // not a class; out of scope; in internal library; private
    final List<String> excludes = Arrays.asList("PI", "InTool", "FixedLengthListMixin", "_Proxy", "_SplayTree", "_InLibPartPrivate",
                                                "_InExported", "_InLib2", "_InTool", "_InPackage1", "_InPackage1");
    checkLookupElements(lookupElements, null, includes, excludes, "### 2nd basic completion ###");
  }

  public void testWithImportPrefixes() throws Exception {
    myFixture.addFileToProject("web/other.dart",
                               "import 'dart:core'; export 'other2.dart' show inOther2; var inOtherHidden, inOther, _inOther;");
    myFixture.addFileToProject("web/other2.dart", "var _inOther2, inOther2Hidden, inOther2;");
    myFixture.addFileToProject("lib/other3.dart", "enum Enum {enumConstant}\n" +
                                                  "typedef void Typedef(param);");
    doTest("import 'dart:core' as core;\n" +
           "import 'other.dart' hide inOtherHidden;\n" +
           "foo() {\n" +
           "  core.<caret completionIncludes='int,Object,String' completionExcludes='core,foo,inOtherHidden,inOther2Hidden,inOther,inOther2,_inOther,_inOther2,Enum,Typedef,JsObject'>x;\n" +
           "  <caret completionIncludes='core,foo,inOther,inOther2' completionExcludes='inOtherHidden,inOther2Hidden,_inOther,_inOther2,Enum,Typedef,int,Object,String,JsObject'>\n" +
           "}");

    final LookupElement[] lookupElements = myFixture.complete(CompletionType.BASIC, 2);
    final List<String> includes =
      Arrays.asList("Object", "String", "int", "bool", "Iterable", "Set", "StateError", "SetMixin", "Point", "JsObject",
                    "core", "foo", "inOther", "inOtherHidden", "inOther2", "inOther2Hidden", "Enum", "Typedef");
    final List<String> excludes = Arrays.asList("_inOther", "_inOther2");
    checkLookupElements(lookupElements, null, includes, excludes, "### 2nd basic completion ###");
  }

  public void testEnums() throws Exception {
    doTest("enum Foo{Foo1, Foo2}\n" +
           "var a = Fo<caret completionEquals='Foo,FormatException'>o;\n" +
           "var b = Foo.<caret completionEquals='Foo1,Foo2,values'>i;\n" +
           "var c = Foo.Foo1.<caret completionEquals='index,toString,hashCode,runtimeType,noSuchMethod'>");
  }
}
