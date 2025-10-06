package org.jetbrains.plugins.cucumber.java.rename;


import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;
import org.jetbrains.plugins.cucumber.java.resolve.BaseCucumberJavaResolveTest;


/// Tests that [GherkinInplaceRenameTest][org.jetbrains.plugins.cucumber.refactoring.rename.GherkinInplaceRenameTest]
/// works fine in a language-specific setting (Java).
///
/// See also: IDEA-376182.
public class CucumberJavaGherkinInplaceRenameTest extends BaseCucumberJavaResolveTest {

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumber7ProjectDescriptor();
  }

  @Override
  protected String getRelatedTestDataPath() {
    return CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "renameStepParameter";
  }

  private void doTest(String newName) {
    myFixture.copyDirectoryToProject(getTestName(true) + "/before", "");
    myFixture.configureByFiles("test.feature");
    myFixture.testHighlighting("test.feature"); // ensure everything is resolved

    myFixture.renameElementAtCaretUsingHandler(newName);

    myFixture.checkResultByFile("test.feature", getTestName(true) + "/after/test.feature", false);
  }

  public void _testParameterUsage() {
    // Disabled because of IDEA-376182.
    doTest("newStart");
  }

  public void testParameterDefinition() {
    doTest("newStart");
  }
}
