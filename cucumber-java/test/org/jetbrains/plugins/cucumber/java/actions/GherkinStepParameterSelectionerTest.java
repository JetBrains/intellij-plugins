package org.jetbrains.plugins.cucumber.java.actions;

import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.CodeInsightTestUtil;
import org.jetbrains.plugins.cucumber.CucumberCodeInsightTestCase;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

@TestDataPath("$CONTENT_ROOT/testData/selectWord")
public class GherkinStepParameterSelectionerTest extends CucumberCodeInsightTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    myFixture.configureByFile("MyStepdefs.java");
  }

  public void testStepWithQuotedString() {
    doTest();
  }

  public void testScenarioStepWithTag() {
    doTest();
  }
  
  private void doTest() {
    CodeInsightTestUtil.doWordSelectionTestOnDirectory(myFixture, getTestName(true), "feature");
  }

  @Override
  protected String getBasePath() {
    return CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "selectWord";
  }
  
    @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumber2ProjectDescriptor();
  }
}
