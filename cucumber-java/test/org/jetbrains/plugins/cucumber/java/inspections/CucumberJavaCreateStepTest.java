package org.jetbrains.plugins.cucumber.java.inspections;

import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

public class CucumberJavaCreateStepTest extends AbstractCucumberJavaCreateStepTest {
  public void testCreateAllSteps() {
    doTest(true);
  }

  public void testStepWithSlash() {
    doTest(false);
  }

  public void testJava8Step() {
    doTest(false);
  }

  public void testCreateAllStepsInGherkin6() {
    doTest(true);
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumberJava8ProjectDescriptor();
  }
}
