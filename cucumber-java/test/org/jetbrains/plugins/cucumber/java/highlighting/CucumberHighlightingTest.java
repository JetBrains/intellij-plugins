package org.jetbrains.plugins.cucumber.java.highlighting;

import com.intellij.testFramework.PlatformTestCase;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.jetbrains.plugins.cucumber.inspections.CucumberStepInspection;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

/**
 * User: Andrey.Vokin
 * Date: 3/1/13
 */
public class CucumberHighlightingTest extends LightPlatformCodeInsightFixtureTestCase {
  public CucumberHighlightingTest() {
    PlatformTestCase.autodetectPlatformPrefix();
  }

  public void testStepParameter() {
    doTest();
  }

  protected void doTest() {
    myFixture.enableInspections(new CucumberStepInspection());
    myFixture.copyDirectoryToProject(getTestName(true), "");
    myFixture.configureByFile(getTestName(true) + "/test.feature");
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
  }

  @Override
  protected boolean isWriteActionRequired() {
    return false;
  }
}
