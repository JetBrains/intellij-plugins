package org.jetbrains.plugins.cucumber.java.highlighting;

import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.plugins.cucumber.inspections.CucumberStepInspection;
import org.jetbrains.plugins.cucumber.java.CucumberJavaCodeInsightTestCase;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

public class CucumberHighlightingTest extends CucumberJavaCodeInsightTestCase {
  public void testStepParameter() {
    doTest();
  }

  public void testScenarioParameter() {
    doTest();
  }

  public void testStepWithDocstring() {
    doTest();
  }

  public void testOrGroup() {
    doTest();
  }

  protected void doTest() {
    myFixture.testHighlighting(true, true, true);
  }

  @Override
  protected String getBasePath() {
    return CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "highlighting";
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.allowTreeAccessForAllFiles();

    myFixture.enableInspections(new CucumberStepInspection());
    myFixture.copyDirectoryToProject(getTestName(true), "");
    myFixture.configureByFile(getTestName(true) + "/test.feature");
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumber3ProjectDescriptor();
  }
}
