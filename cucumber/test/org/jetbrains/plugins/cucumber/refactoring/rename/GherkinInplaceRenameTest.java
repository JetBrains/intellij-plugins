package org.jetbrains.plugins.cucumber.refactoring.rename;

import com.intellij.openapi.command.CommandProcessor;
import com.intellij.refactoring.RefactoringSettings;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.CodeInsightTestUtil;
import org.jetbrains.plugins.cucumber.CucumberTestUtil;
import org.jetbrains.plugins.cucumber.psi.refactoring.rename.GherkinInplaceRenameHandler;

public class GherkinInplaceRenameTest extends BasePlatformTestCase {
  private static final String TEST_DATA_PATH = "/refactoring/rename";

  public void testRenameStepParameter() {
    doTest("newDescription");
  }

  private void doTest(String newName) {
    boolean b = RefactoringSettings.getInstance().RENAME_SEARCH_IN_COMMENTS_FOR_FILE;
    try {
      myFixture.configureByFile(getTestName(true) + ".feature");
      CommandProcessor.getInstance().executeCommand(
        getProject(),
        () -> CodeInsightTestUtil.doInlineRename(new GherkinInplaceRenameHandler(), newName, myFixture), "Rename", null);

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
