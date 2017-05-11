package org.jetbrains.plugins.cucumber.java.inspections;

import com.intellij.codeInspection.deadCode.UnusedDeclarationInspectionBase;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

public class UnusedJavaMethodInspectionTest extends CucumberJavaBaseInspectionTest {
  protected void doTest(final String file) {
    myFixture.enableInspections(new UnusedDeclarationInspectionBase(true));
    myFixture.configureByFile(file);
    myFixture.testHighlighting(true, false, true);
  }

  public void testStepDefinition() {
    doTest("ShoppingStepdefs.java");
  }

  public void testPrivateMethod() {
    doTest("PrivateMethod.java");
  }

  public void testHooks() {
    doTest("Hooks.java");
  }

  @Override
  protected String getBasePath() {
    return CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "inspections/unusedMethod";
  }
}
