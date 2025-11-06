package org.jetbrains.plugins.cucumber.java.inspections;

import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.plugins.cucumber.inspections.GherkinBrokenTableInspection;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

public class GherkinBrokenTableInspectionTest extends BasePlatformTestCase {
  public void testBrokenTable() {
    doTest();
  }

  protected void doTest() {
    myFixture.enableInspections(new GherkinBrokenTableInspection());
    myFixture.copyDirectoryToProject(getTestName(true), "");
    myFixture.configureByFile(getTestName(true) + "/test.feature");
    myFixture.testHighlighting(true, true, true);
  }

  @Override
  protected String getBasePath() {
    return CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "inspections";
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumber2ProjectDescriptor();
  }
}
