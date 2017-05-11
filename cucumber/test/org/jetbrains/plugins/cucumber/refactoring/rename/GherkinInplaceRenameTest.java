package org.jetbrains.plugins.cucumber.refactoring.rename;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.refactoring.RefactoringSettings;
import com.intellij.testFramework.fixtures.CodeInsightTestUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.jetbrains.plugins.cucumber.CucumberTestUtil;
import org.jetbrains.plugins.cucumber.psi.refactoring.rename.GherkinInplaceRenameHandler;

public class GherkinInplaceRenameTest extends LightPlatformCodeInsightFixtureTestCase {
  private static final String TEST_DATA_PATH = "/refactoring/rename";

  public void testRenameStepParameter() {
    doTest("newDescription");
  }

  private void doTest(String newName) {
    boolean b = RefactoringSettings.getInstance().RENAME_SEARCH_IN_COMMENTS_FOR_FILE;
    try {
      myFixture.configureByFile(getTestName(true) + ".feature");
      WriteCommandAction.runWriteCommandAction(getProject(), () -> CodeInsightTestUtil.doInlineRename(new GherkinInplaceRenameHandler(), newName, myFixture));

      myFixture.checkResultByFile(getTestName(true) + "_after.feature");
    }
    finally {
      RefactoringSettings.getInstance().RENAME_SEARCH_IN_COMMENTS_FOR_FILE = b;
    }
  }

  @Override
  protected String getTestDataPath() {
    return CucumberTestUtil.getTestDataPath() + TEST_DATA_PATH;
  }
}
