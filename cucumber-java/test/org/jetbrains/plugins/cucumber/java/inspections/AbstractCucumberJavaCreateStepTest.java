// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.inspections;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.plugins.cucumber.inspections.CucumberStepInspection;
import org.jetbrains.plugins.cucumber.java.CucumberJavaCodeInsightTestCase;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

public abstract class AbstractCucumberJavaCreateStepTest extends CucumberJavaCodeInsightTestCase {
  protected void doTest(boolean createAll) {
    myFixture.enableInspections(new CucumberStepInspection());
    myFixture.copyDirectoryToProject("createStep/" + getTestName(true) , "");
    myFixture.configureByFile("createStep/" + getTestName(true) + "/test.feature");

    myFixture.checkHighlighting(true, false, false);

    final String fixName = createAll ? "Create all" : "Create step";
    final IntentionAction quickFix = ContainerUtil.find(myFixture.getAvailableIntentions(),
                                                        intentionAction -> intentionAction.getText().contains(fixName));

    if (quickFix != null) {
      myFixture.launchAction(quickFix);
      VirtualFile expectedFile = myFixture.findFileInTempDir("StepDefs.java");
      myFixture.openFileInEditor(expectedFile);
      myFixture.checkResultByFile("createStep/" + getTestName(true) + "/StepDefs_fixed.txt");
    }
    else {
      fail("Quick fix not found");
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
}
