package org.jetbrains.plugins.cucumber.java.rename;

import com.intellij.idea.TestFor;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;
import org.jetbrains.plugins.cucumber.refactoring.rename.GherkinStepParameterRenameTest;

/// Tests that [GherkinStepParameterRenameTest][GherkinStepParameterRenameTest]
/// works fine in a language-specific setting (Java).
///
/// The "language-specific-setting" part matters! To learn why, see IDEA-376182.
public class CucumberJavaStepParameterRenameTest extends BasePlatformTestCase {

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumber7ProjectDescriptor();
  }

  @Override
  protected String getBasePath() {
    return CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "renameStepParameter";
  }

  private void doTest(String newName) {
    myFixture.copyDirectoryToProject(getTestName(true) + "/before", "");
    myFixture.configureByFile("test.feature");
    myFixture.testHighlighting("test.feature"); // ensure everything is resolved

    myFixture.renameElementAtCaretUsingHandler(newName);

    myFixture.checkResultByFile("test.feature", getTestName(true) + "/after/test.feature", false);
  }

  public void testParameterDefinition() {
    doTest("newStart");
  }

  @TestFor(issues = "IDEA-376182")
  public void testParameterUsage() {
    doTest("newStart");
  }

  @TestFor(issues = "IDEA-376182")
  public void testParameterUsageOnUnresolvedStep() {
    doTest("newStart");
  }

  @TestFor(issues = "IDEA-374108")
  public void testParameterUsageWithCaretJustBeforeClosingBracket() {
    doTest("helloDarkness");
  }
}
