package org.jetbrains.plugins.cucumber.completion;

import com.intellij.testFramework.fixtures.CompletionTester;
import org.jetbrains.plugins.cucumber.CucumberCodeInsightTestCase;
import org.jetbrains.plugins.cucumber.CucumberTestUtil;
import org.jetbrains.plugins.cucumber.psi.GherkinFileType;

import java.io.File;

public class CucumberCompletionVariantsTest extends CucumberCodeInsightTestCase {
  private CompletionTester myCompletionTester;

  public void testScenarioPriority() throws Throwable {
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
