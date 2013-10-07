package org.jetbrains.plugins.cucumber.java.actions;

import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.CodeInsightTestUtil;
import org.jetbrains.plugins.cucumber.CucumberCodeInsightTestCase;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

/**
 * User: zolotov
 * Date: 10/7/13
 */
@TestDataPath("$CONTENT_ROOT/testData/selectWord")
public class GherkinStepParameterSelectionerTest extends CucumberCodeInsightTestCase {

  public void testStepWithQuotedString() throws Exception {
    myFixture.configureByFile("MyStepdefs.java");
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
    return CucumberJavaTestUtil.createCucumberProjectDescriptor();
  }
}
