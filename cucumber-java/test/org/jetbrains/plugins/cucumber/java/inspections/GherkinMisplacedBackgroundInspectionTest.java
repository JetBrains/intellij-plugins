package org.jetbrains.plugins.cucumber.java.inspections;

import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.plugins.cucumber.inspections.GherkinMisplacedBackgroundInspection;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

public class GherkinMisplacedBackgroundInspectionTest extends BasePlatformTestCase {
  public void testMisplacedBackground() {
    doTest();
  }

  protected void doTest() {
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
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumber2ProjectDescriptor();
  }
}

