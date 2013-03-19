package org.jetbrains.plugins.cucumber.java.completion;

/**
 * User: Andrey.Vokin
 * Date: 3/14/13
 */

import com.intellij.testFramework.fixtures.CompletionTester;
import org.jetbrains.plugins.cucumber.CucumberCodeInsightTestCase;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;
import org.jetbrains.plugins.cucumber.psi.GherkinFileType;

import java.io.File;

public class CucumberJavaCompletionTest extends CucumberCodeInsightTestCase {
  private CompletionTester myCompletionTester;

  public void testStepWithRegExGroups() throws Throwable {
    doTestVariants();
  }

  private void doTestVariants() throws Throwable {
    myFixture.copyDirectoryToProject(getTestName(true), "");
    myCompletionTester.doTestVariantsInner(getTestName(true) + "/" + getTestName(true) + ".feature", GherkinFileType.INSTANCE);
  }

  @Override
  protected String getBasePath() {
    return CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "completion" + File.separator;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myCompletionTester = new CompletionTester(myFixture);
  }
}
