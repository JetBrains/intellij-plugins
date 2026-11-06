package org.jetbrains.plugins.cucumber.completion;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.CompletionTester;
import org.jetbrains.plugins.cucumber.CucumberTestUtil;
import org.jetbrains.plugins.cucumber.psi.GherkinFileType;

import java.io.File;

public class CucumberCompletionVariantsTest extends BasePlatformTestCase {
  private CompletionTester myCompletionTester;

  public void testScenarioPriority() throws Throwable {
    doTestVariants();
  }

  public void testInRule() throws Throwable {
    doTestVariants();
  }

  // Any scenario may get Examples (since Gherkin 6): the keyword is offered in a plain Scenario: ...
  public void testExamplesInScenario() throws Throwable {
    doTestVariants();
  }

  // ... and in a Scenario Outline: that has no Examples yet, which is a plain scenario until it gets one.
  public void testExamplesInOutlineWithoutExamples() throws Throwable {
    doTestVariants();
  }

  private void doTestVariants() throws Throwable {
    myCompletionTester.doTestVariantsInner(getTestName(true) + ".feature", GherkinFileType.INSTANCE);
  }

  @Override
  protected String getTestDataPath() {
    return CucumberTestUtil.getTestDataPath() + File.separator + "completion/variants" + File.separator;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myCompletionTester = new CompletionTester(myFixture);
  }
}
