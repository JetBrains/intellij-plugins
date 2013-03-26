package org.jetbrains.plugins.cucumber.java.inspections;

import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

/**
 * User: Andrey.Vokin
 * Date: 1/9/13
 */
public class CucumberJavaStepDefClassInDefaultPackageInspectionTest extends CucumberJavaBaseInspectionTest {
  public void testStepDefClassInDefaultPackage() {
    doTest("StepDefClassInDefaultPackage.java");
  }

  public void testStepDefClassInNamedPackage() {
    doTest("StepDefClassInNamedPackage.java");
  }

  protected void doTest(final String file) {
    myFixture.enableInspections(new CucumberJavaStepDefClassInDefaultPackageInspection());
    myFixture.configureByFile(file);
    myFixture.testHighlighting(true, false, true);
  }

  @Override
  protected String getBasePath() {
    return CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "inspections/stepDefClassInDefaultPackage";
  }
}
