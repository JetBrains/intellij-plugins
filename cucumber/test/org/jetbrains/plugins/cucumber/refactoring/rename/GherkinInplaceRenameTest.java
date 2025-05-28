package org.jetbrains.plugins.cucumber.refactoring.rename;

import com.intellij.openapi.command.CommandProcessor;
import com.intellij.refactoring.RefactoringSettings;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.CodeInsightTestUtil;
import org.jetbrains.plugins.cucumber.CucumberTestUtil;
import org.jetbrains.plugins.cucumber.psi.refactoring.rename.GherkinParameterRenameHandler;

public class GherkinInplaceRenameTest extends BasePlatformTestCase {
  private static final String TEST_DATA_PATH = "/refactoring/rename";

  // IDEA-107390
  public void ignore_testRenameStep_1() {
    // Renaming steps must be implemented by a language plugin (like cucumber-java),
    // because renaming step usage doesn't make sense if the corresponding step definition
    // isn't also updated.
    doTest("I ask whether it's weekend");
  }

  public void testRenameParameter_1() {
    doTest("newDescription");
  }

  public void testRenameParameter_2() {
    doTest("subtract_amount");
  }

  private void doTest(String newName) {
    boolean b = RefactoringSettings.getInstance().RENAME_SEARCH_IN_COMMENTS_FOR_FILE;
    try {
      myFixture.configureByFile(getTestName(true) + ".feature");
      CommandProcessor.getInstance().executeCommand(
        getProject(),
        () -> CodeInsightTestUtil.doInlineRename(new GherkinParameterRenameHandler(), newName, myFixture), "Rename", null);

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
