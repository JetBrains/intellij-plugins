package org.jetbrains.plugins.cucumber.java.inspections;

import org.jetbrains.plugins.cucumber.inspections.GherkinMisplacedBackgroundInspection;
import org.jetbrains.plugins.cucumber.java.CucumberJavaCodeInsightTestCase;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;
import org.jetbrains.plugins.cucumber.steps.CucumberStepsIndex;

/**
 * User: Andrey.Vokin
 * Date: 10/3/2014.
 */
public class GherkinMisplacedBackgroundInspectionTest extends CucumberJavaCodeInsightTestCase {
  public void testMisplacedBackground() {
    doTest();
  }

  protected void doTest() {
    CucumberStepsIndex.getInstance(getProject()).reset();
    myFixture.enableInspections(new GherkinMisplacedBackgroundInspection());
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

  @Override
  protected boolean isWriteActionRequired() {
    return false;
  }
}

