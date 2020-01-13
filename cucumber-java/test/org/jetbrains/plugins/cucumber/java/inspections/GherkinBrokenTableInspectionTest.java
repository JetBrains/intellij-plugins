package org.jetbrains.plugins.cucumber.java.inspections;

import org.jetbrains.plugins.cucumber.inspections.GherkinBrokenTableInspection;
import org.jetbrains.plugins.cucumber.java.CucumberJavaCodeInsightTestCase;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

public class GherkinBrokenTableInspectionTest extends CucumberJavaCodeInsightTestCase {
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
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.allowTreeAccessForAllFiles();
  }
}
