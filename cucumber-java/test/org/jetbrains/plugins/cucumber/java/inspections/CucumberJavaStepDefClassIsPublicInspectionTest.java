package org.jetbrains.plugins.cucumber.java.inspections;

import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

/**
 * User: Andrey.Vokin
 * Date: 1/9/13
 */
public class CucumberJavaStepDefClassIsPublicInspectionTest extends CucumberJavaBaseInspectionTest {
  public void testNotPublicStepDef() {
    doTest("NotPublicStepDef.java");
  }

  public void testPublicStepDef() {
    doTest("PublicStepDef.java");
  }

  public void testNotPublicNotStepDef() {
    doTest("NotPublicNotStepDef.java");
  }

  protected void doTest(final String file) {
    myFixture.enableInspections(new CucumberJavaStepDefClassIsPublicInspections());
    myFixture.configureByFile(file);
    myFixture.testHighlighting(true, false, true);
  }

  @Override
  protected String getBasePath() {
    return CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "inspections\\stepDefClassIsPublic";
  }
}
