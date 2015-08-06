package com.jetbrains.dart.analysisServer;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupEx;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.openapi.util.Condition;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.jetbrains.annotations.Nullable;

public class DartServerCompletionTest extends CodeInsightFixtureTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    final DartSdk sdk = DartTestUtils.configureDartSdk(myModule, getTestRootDisposable(), true);
    DartAnalysisServerService.getInstance().serverReadyForRequest(getProject(), sdk);
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
      myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR);
    }

    myFixture.checkResultByFile(getTestName(false) + ".after.dart");
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
    doTest("for ()");
  }
}
