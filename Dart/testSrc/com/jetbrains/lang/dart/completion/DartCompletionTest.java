package com.jetbrains.lang.dart.completion;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.util.CaretPositionInfo;
import com.jetbrains.lang.dart.util.DartTestUtils;

import java.util.ArrayList;
import java.util.List;

public class DartCompletionTest extends DartCodeInsightFixtureTestCase {

  @Override
  protected String getBasePath() {
    return "/completion";
  }

  private void doTest(final String text) {
    final PsiFile file = myFixture.addFileToProject("web/" + getTestName(true) + ".dart", text);
    myFixture.openFileInEditor(file.getVirtualFile());

    final List<CaretPositionInfo> caretPositions = DartTestUtils.extractPositionMarkers(getProject(), myFixture.getEditor().getDocument());

    for (CaretPositionInfo caretPositionInfo : caretPositions) {
      myFixture.getEditor().getCaretModel().moveToOffset(caretPositionInfo.caretOffset);
      final LookupElement[] lookupElements = myFixture.completeBasic();
      checkLookupElements(lookupElements,
                          caretPositionInfo.completionEqualsList,
                          caretPositionInfo.completionIncludesList,
                          caretPositionInfo.completionExcludesList);
      LookupManager.getInstance(getProject()).hideActiveLookup();
    }
  }

  private static void checkLookupElements(final LookupElement[] lookupElements,
                                          final List<String> equalsList,
                                          final List<String> includesList,
                                          final List<String> excludesList) {
    final List<String> lookupStrings = new ArrayList<String>();
    for (LookupElement element : lookupElements) {
      lookupStrings.add(element.getLookupString());
    }

    if (equalsList != null) {
      assertSameElements(lookupStrings, equalsList);
    }

    if (includesList != null) {
      for (String s : includesList) {
        assertTrue(s, lookupStrings.contains(s));
      }
    }

    if (excludesList != null) {
      for (String s : excludesList) {
        assertFalse(s, lookupStrings.contains(s));
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
}
