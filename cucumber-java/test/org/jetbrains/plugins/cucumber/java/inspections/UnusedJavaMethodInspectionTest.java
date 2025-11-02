package org.jetbrains.plugins.cucumber.java.inspections;

import com.intellij.codeInspection.deadCode.UnusedDeclarationInspectionBase;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

public class UnusedJavaMethodInspectionTest extends BasePlatformTestCase {

  protected void doTest(String file) {
    myFixture.enableInspections(new UnusedDeclarationInspectionBase(true));
    myFixture.copyDirectoryToProject(".", "");
    myFixture.configureByFile(file);
    myFixture.testHighlighting();
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

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumber7ProjectDescriptor();
  }
}
