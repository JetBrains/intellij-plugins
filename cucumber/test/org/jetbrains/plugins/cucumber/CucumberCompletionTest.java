package org.jetbrains.plugins.cucumber;

import com.intellij.testFramework.fixtures.CompletionTester;
import org.jetbrains.plugins.cucumber.psi.GherkinFileType;

import java.io.File;

/**
 * User: Andrey.Vokin
 * Date: 3/13/13
 */
public class CucumberCompletionTest extends CucumberCodeInsightTestCase {
  private CompletionTester myCompletionTester;

  public void testScenarioPriority() throws Throwable {
    doTestVariants();
  }

  private void doTestVariants() throws Throwable {
    myCompletionTester.doTestVariantsInner(getTestName(false) + ".feature", GherkinFileType.INSTANCE);
  }

  @Override
  protected String getTestDataPath() {
    return CucumberTestUtil.getTestDataPath() + File.separator + "completion" + File.separator;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myCompletionTester = new CompletionTester(myFixture);
  }
}
