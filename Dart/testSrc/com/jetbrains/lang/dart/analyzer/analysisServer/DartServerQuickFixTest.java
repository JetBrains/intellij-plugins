package com.jetbrains.lang.dart.analyzer.analysisServer;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class DartServerQuickFixTest extends CodeInsightFixtureTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule, getTestRootDisposable());
    myFixture.setTestDataPath(DartTestUtils.BASE_TEST_DATA_PATH + getBasePath());
  }

  protected String getBasePath() {
    return "/analysisServer/quickfix";
  }

  private void doQuickFixTest(@NotNull final String intentionStartText) {
    doQuickFixTest(intentionStartText, null, true);
  }

  private void doQuickFixTest(@NotNull final String intentionStartText, @Nullable final VirtualFile fileUpdatedByFix) {
    doQuickFixTest(intentionStartText, fileUpdatedByFix, true);
  }

  /**
   * @param fileUpdatedByFix <code>null</code> if quick fix updates the file from which it was invoked
   */
  private void doQuickFixTest(@NotNull final String intentionStartText,
                              @Nullable final VirtualFile fileUpdatedByFix,
                              boolean fixAvailable) {
    final PsiFile initialFile = myFixture.configureByFile(getTestName(false) + ".dart");

    final IntentionAction quickFix = myFixture.findSingleIntention(intentionStartText);
    assertEquals(fixAvailable, quickFix.isAvailable(getProject(), getEditor(), getFile()));
    if (!fixAvailable) return;

    if (fileUpdatedByFix != null) {
      // todo This is a workaround because BaseCreateFix.navigate() that is called in DartServerFixIntention.invoke() behaves differently in test environment.
      // Would be great to remove this code and instead check that correct editor becomes open after fix.invoke()
      myFixture.openFileInEditor(fileUpdatedByFix);
    }

    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        quickFix.invoke(getProject(), getEditor(), getFile());
      }
    });

    final String updatedFileName = fileUpdatedByFix == null ? getTestName(false) : fileUpdatedByFix.getNameWithoutExtension();
    myFixture.checkResultByFile(updatedFileName + ".after.dart");
  }

  public void testCreateMethodInAnotherFile() throws Throwable {
    final VirtualFile partFile = myFixture.copyFileToProject(getTestName(false) + "_part.dart");
    doQuickFixTest("Create method 'doSomething'", partFile);
  }
}
