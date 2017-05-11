package org.jetbrains.plugins.cucumber.java.inspections;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.ex.QuickFixWrapper;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.plugins.cucumber.inspections.CucumberStepInspection;
import org.jetbrains.plugins.cucumber.java.CucumberJavaCodeInsightTestCase;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;
import org.jetbrains.plugins.cucumber.steps.CucumberStepsIndex;

public class CucumberJavaCreateStepTest extends CucumberJavaCodeInsightTestCase {
  public void testCreateAllSteps() {
    doTest(true);
  }

  public void testJava8Step() {
    doTest(false);
  }

  private void doTest(boolean createAll) {
    CucumberStepsIndex.getInstance(getProject()).reset();
    myFixture.enableInspections(new CucumberStepInspection());
    myFixture.copyDirectoryToProject("createStep/" + getTestName(true) , "");
    myFixture.configureByFile("createStep/" + getTestName(true) + "/test.feature");

    myFixture.checkHighlighting(true, false, false);

    final String fixName = createAll ? "Create All" : "Create step";
    final IntentionAction quickFix = ContainerUtil.find(myFixture.getAvailableIntentions(),
                                                        intentionAction -> intentionAction instanceof QuickFixWrapper && intentionAction.getText().contains(fixName));

    if (quickFix != null) {
      myFixture.launchAction(quickFix);
      VirtualFile expectedFile = myFixture.findFileInTempDir("StepDefs.java");
      myFixture.openFileInEditor(expectedFile);
      myFixture.checkResultByFile("createStep/" + getTestName(true) + "/StepDefs_fixed.txt");
    }
  }

  @Override
  protected String getBasePath() {
    return CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "inspections";
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.allowTreeAccessForAllFiles();
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumberJava8ProjectDescriptor();
  }
}
