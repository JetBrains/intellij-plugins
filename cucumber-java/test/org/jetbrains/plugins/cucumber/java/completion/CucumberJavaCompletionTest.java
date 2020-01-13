package org.jetbrains.plugins.cucumber.java.completion;


import com.intellij.testFramework.fixtures.CompletionTester;
import org.jetbrains.plugins.cucumber.java.CucumberJavaCodeInsightTestCase;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;
import org.jetbrains.plugins.cucumber.psi.GherkinFileType;

import java.io.File;

public class CucumberJavaCompletionTest extends CucumberJavaCodeInsightTestCase {
  private CompletionTester myCompletionTester;

  @Override
  protected void tearDown() throws Exception {
    myCompletionTester = null;
    super.tearDown();
  }

  public void testStepWithRegExGroups() throws Throwable {
    doTestVariants();
  }

  public void testStepWithRegex() throws Throwable {
    doTestVariants();
  }

  public void testStepWithQuestionMark() throws Throwable {
    doTestVariants();
  }

  public void testStepWithInterpolation() throws Throwable {
    doTestVariants();
  }

  public void testStepWithGroupInsideGroup() throws Throwable {
    doTestVariants();
  }

  public void testStepWithNumberStartingWithDot() throws Throwable {
    doTestVariants();
  }

  public void testWordOrder() throws Throwable {
    doTestVariants();
  }

  public void testCompletionForNonCapturingTokens() throws Throwable {
    doTestVariants();
  }

  public void testCompletionForOrGroup() throws Throwable {
    doTestVariants();
  }

  public void testCompletionForInt() throws Throwable {
    doTestVariants();
  }

  public void testNoCompletionInTable() throws Throwable {
    doTestVariants();
  }

  private void doTestVariants() throws Throwable {
    myFixture.copyDirectoryToProject(getTestName(true), "");
    myCompletionTester.doTestVariantsInner(getTestName(true) + File.separator + getTestName(true) + ".feature", GherkinFileType.INSTANCE);
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
