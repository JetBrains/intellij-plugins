package org.jetbrains.plugins.cucumber.java.inspections;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.ex.QuickFixWrapper;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.plugins.cucumber.inspections.CucumberStepInspection;
import org.jetbrains.plugins.cucumber.java.CucumberJavaCodeInsightTestCase;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;
import org.jetbrains.plugins.cucumber.steps.CucumberStepsIndex;

/**
 * User: Andrey.Vokin
 * Date: 10/8/2014.
 */
public class CucumberJavaCreateStepTest extends CucumberJavaCodeInsightTestCase {
  public void testCreateAllSteps() {
    doTest(true);
  }

  private void doTest(boolean createAll) {
    CucumberStepsIndex.getInstance(getProject()).reset();
    myFixture.enableInspections(new CucumberStepInspection());
    myFixture.copyDirectoryToProject("createStep", "");
    myFixture.configureByFile("createStep/" + getTestName(true) + ".feature");

    myFixture.checkHighlighting(true, false, false);

    final String fixName = createAll ? "Create All" : "Create step";
    final IntentionAction quickFix = ContainerUtil.find(myFixture.getAvailableIntentions(), new Condition<IntentionAction>() {
      @Override
      public boolean value(final IntentionAction intentionAction) {
        return intentionAction instanceof QuickFixWrapper && intentionAction.getText().contains(fixName);
      }
    });

    if (quickFix != null) {
      myFixture.launchAction(quickFix);
      VirtualFile expectedFile = myFixture.findFileInTempDir("CreateAllStepDefs.java");
      myFixture.openFileInEditor(expectedFile);
      myFixture.checkResultByFile("createStep/CreateAllStepDefs_fixed.java");
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
  protected boolean isWriteActionRequired() {
    return false;
  }
}
