package com.jetbrains.dart.analysisServer;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupEx;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.EditorTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartServerCompletionTest extends CodeInsightFixtureTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule, getTestRootDisposable(), true);
    DartAnalysisServerService.getInstance().serverReadyForRequest(getProject());
    myFixture.setTestDataPath(DartTestUtils.BASE_TEST_DATA_PATH + getBasePath());
  }

  protected String getBasePath() {
    return "/analysisServer/completion";
  }

  private void doTest() {
    doTest(null);
  }

  private void doTest(@Nullable final String lookupToSelect) {
    myFixture.configureByFile(getTestName(false) + ".dart");
    myFixture.complete(CompletionType.BASIC);

    if (lookupToSelect != null) {
      selectLookup(lookupToSelect, Lookup.NORMAL_SELECT_CHAR);
    }

    myFixture.checkResultByFile(getTestName(false) + ".after.dart");
  }

  private void selectLookup(@NotNull final String lookupToSelect, final char completionChar) {
    final LookupEx activeLookup = LookupManager.getActiveLookup(getEditor());
    assertNotNull(activeLookup);

    final LookupElement lookup = ContainerUtil.find(activeLookup.getItems(), new Condition<LookupElement>() {
      @Override
      public boolean value(LookupElement element) {
        return lookupToSelect.equals(element.getLookupString());
      }
    });

    assertNotNull(lookupToSelect + " is not in the completion list", lookup);

    activeLookup.setCurrentItem(lookup);
    myFixture.finishLookup(completionChar);
  }

  public void testFunctionWithArgsInvocation() throws Throwable {
    doTest();
  }

  public void testFunctionNoArgsInvocation() throws Throwable {
    doTest();
  }

  public void testFunctionAfterShow() throws Throwable {
    doTest();
  }

  public void testFunctionAsArgument() throws Throwable {
    doTest();
  }

  public void testCaretPlacementInFor() throws Throwable {
    doTest("for");
  }

  public void testWithImportPrefix() throws Throwable {
    doTest();
  }

  public void testUriCompletionByTab() throws Throwable {
    final String testName = getTestName(false);
    myFixture.copyDirectoryToProject(testName, testName);

    final VirtualFile root = ModuleRootManager.getInstance(myModule).getContentRoots()[0];
    final VirtualFile file = VfsUtilCore.findRelativeFile(testName + "/web/foo.dart", root);
    assertNotNull(file);
    myFixture.openFileInEditor(file);

    final EditorTestUtil.CaretAndSelectionState markers = EditorTestUtil.extractCaretAndSelectionMarkers(getEditor().getDocument());
    getEditor().getCaretModel().moveToOffset(markers.carets.get(0).getCaretOffset(getEditor().getDocument()));

    myFixture.complete(CompletionType.BASIC);
    selectLookup("package:projectName/libFile.dart", Lookup.REPLACE_SELECT_CHAR);
    myFixture.checkResultByFile(testName + ".after.dart");
  }
}
