package org.jetbrains.plugins.cucumber.refactoring.rename;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.plugins.cucumber.CucumberTestUtil;

public class GherkinInplaceRenameTest extends BasePlatformTestCase {

  @Override
  protected String getTestDataPath() {
    return CucumberTestUtil.getTestDataPath() + "/refactoring/rename";
  }

  private void doTest(String newName) {
    myFixture.configureByFile(getTestName(true) + ".feature");

    myFixture.renameElementAtCaretUsingHandler(newName);
    myFixture.checkResultByFile(getTestName(true) + ".after.feature");
  }

  public void testRenameParameterUsage() {
    doTest("newStart");
  }

  public void testRenameParameterDefinition() {
    doTest("newStart");
  }
}
