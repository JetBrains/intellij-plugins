// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.dart.analysisServer;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupEx;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.EditorTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DartServerCompletionTest extends CodeInsightFixtureTestCase {
  @Override
  public void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule, myFixture.getTestRootDisposable(), true);
    DartAnalysisServerService.getInstance(getProject()).serverReadyForRequest();
    myFixture.setTestDataPath(DartTestUtils.BASE_TEST_DATA_PATH + getBasePath());
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
  }

  @Override
  protected String getBasePath() {
    return "/analysisServer/completion";
  }

  private void doTest() {
    doTest(null, Lookup.NORMAL_SELECT_CHAR);
  }

  private void doTest(@Nullable final String lookupToSelect) {
    doTest(lookupToSelect, Lookup.NORMAL_SELECT_CHAR);
  }

  private void doTest(@Nullable final String lookupToSelect, final char completionChar) {
    myFixture.configureByFile(getTestName(false) + ".dart");
    myFixture.doHighlighting(); // warm up
    myFixture.complete(CompletionType.BASIC);

    if (lookupToSelect != null) {
      selectLookup(lookupToSelect, completionChar);
    }

    myFixture.checkResultByFile(getTestName(false) + ".after.dart");
  }

  private void selectLookup(@NotNull final String lookupToSelect, final char completionChar) {
    final LookupEx activeLookup = LookupManager.getActiveLookup(getEditor());
    assertNotNull(activeLookup);

    final LookupElement lookup = ContainerUtil.find(activeLookup.getItems(), element -> lookupToSelect.equals(element.getLookupString()));

    assertNotNull(lookupToSelect + " is not in the completion list", lookup);

    activeLookup.setCurrentItem(lookup);
    myFixture.finishLookup(completionChar);
  }

  public void testFunctionWithArgsInvocation() {
    doTest("identical");
  }

  public void testKeepOldArgsOnTab() {
    doTest("identical", Lookup.REPLACE_SELECT_CHAR);
  }

  public void testArgsPlaceholderOnTab() {
    doTest("identical", Lookup.REPLACE_SELECT_CHAR);
  }

  public void testEatTailOnTab() {
    doTest("hashCode", Lookup.REPLACE_SELECT_CHAR);
  }

  public void testDoNotEatParenOnTab() {
    doTest("hashCode", Lookup.REPLACE_SELECT_CHAR);
  }

  public void testDoNotEatParenOnTab2() {
    doTest("hashCode", Lookup.REPLACE_SELECT_CHAR);
  }

  public void testDoNotEatTailOnTab() {
    doTest("parse", Lookup.REPLACE_SELECT_CHAR);
  }

  public void testDoNotEatAwaitOnTab() {
    doTest("fooBar", Lookup.REPLACE_SELECT_CHAR);
  }

  public void testDoNotEatListOnTab() {
    doTest("hashCode", Lookup.REPLACE_SELECT_CHAR);
  }

  public void testDoNotEatMapOnTab() {
    doTest("runtimeType", Lookup.REPLACE_SELECT_CHAR);
  }

  public void testFunctionNoArgsInvocation() {
    doTest();
  }

  // Fails due to https://github.com/dart-lang/sdk/issues/47993
  public void _testFunctionAfterShow() {
    doTest();
  }

  public void testFunctionAsArgument() {
    doTest();
  }

  public void testCaretPlacementInFor() {
    doTest("for");
  }

  public void testWithImportPrefix() {
    doTest();
  }

  public void testInsideIncompleteListLiteral() {
    myFixture.configureByFile(getTestName(false) + ".dart");
    myFixture.doHighlighting();
    myFixture.complete(CompletionType.BASIC);
    assertNotNull(myFixture.getLookup());
  }

  public void testUriCompletionByTab() {
    final String testName = getTestName(false);
    myFixture.copyDirectoryToProject(testName, testName);

    final VirtualFile root = ModuleRootManager.getInstance(myModule).getContentRoots()[0];
    final VirtualFile file = VfsUtilCore.findRelativeFile(testName + "/web/foo.dart", root);
    assertNotNull(file);
    myFixture.openFileInEditor(file);

    final EditorTestUtil.CaretAndSelectionState markers = EditorTestUtil.extractCaretAndSelectionMarkers(getEditor().getDocument());
    getEditor().getCaretModel().moveToOffset(markers.carets().get(0).getCaretOffset(getEditor().getDocument()));

    myFixture.doHighlighting();
    myFixture.complete(CompletionType.BASIC);
    selectLookup("package:projectName/libFile.dart", Lookup.REPLACE_SELECT_CHAR);
    myFixture.checkResultByFile(testName + ".after.dart");
  }

  public void testIncompleteTernary() {
    doTest("true");
  }

  public void testSorting() {
    myFixture.configureByText("foo.dart",
                              """
                                enum AXX {one, two}
                                enum AXB {three, four}
                                void foo({AXX x}) {}
                                main() {
                                  foo(x: <caret>);
                                }""");
    myFixture.doHighlighting();
    myFixture.completeBasic();
    myFixture.assertPreferredCompletionItems(0, "AXX", "AXX.one", "AXX.two", "const", "true", "false",
                                             "AXB", "AXB.four", "AXB.three", "null", "main",
                                             "AbstractClassInstantiationError", "ArgumentError",
                                             "ArgumentError.notNull", "ArgumentError.value",
                                             "AssertionError", "BidirectionalIterator");
  }

  public void testNoCompletionAfterDigit() {
    myFixture.configureByText("foo.dart",
                              """
                                main() {
                                  return Foo(bar: 1<caret>, );
                                }
                                class Foo {
                                  final double bar, baz;
                                  const Foo({ this.bar, this.baz, });
                                }
                                """);
    myFixture.doHighlighting();
    myFixture.completeBasic();
    assertNull(myFixture.getLookup());
  }

  public void testNotYetImportedClass() {
    doTest();
  }

  public void testExistingImports() {
    myFixture.configureByFiles(getTestName(false) + ".dart", "ExistingImportLibrary.dart");
    myFixture.doHighlighting();
    myFixture.complete(CompletionType.BASIC);
    final LookupEx activeLookup = LookupManager.getActiveLookup(getEditor());
    assertNotNull(activeLookup);
    final List<LookupElement> matches = ContainerUtil.findAll(
      activeLookup.getItems(), element -> element.getLookupString().equals("Process"));
    // Since we've already imported ExistingImportLibrary.dart which re-exports Process, we should only have one suggestion for it.
    assertEquals(1, matches.size());
    activeLookup.setCurrentItem(matches.get(0));
    myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR);
    myFixture.checkResultByFile(getTestName(false) + ".after.dart");
  }

  public void testConstructorParens() { doTest(); }
}
