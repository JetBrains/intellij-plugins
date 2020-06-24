// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.execution.TestStateStorage;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.execution.testframework.sm.runner.states.TestStateInfo;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiFile;
import org.jetbrains.plugins.cucumber.java.CucumberJavaCodeInsightTestCase;
import org.jetbrains.plugins.cucumber.run.CucumberRunLineMarkerContributor;

import java.util.Date;

public class CucumberFeatureRunLineContributorTest extends CucumberJavaCodeInsightTestCase {
  public void testFeatureRunLineContributorWhenFeatureNeverHaveBeenRan() {
    PsiFile file = myFixture.configureByText("test.feature", "Feature: My feature");
    RunLineMarkerContributor.Info info = new CucumberRunLineMarkerContributor().getInfo(file.findElementAt(myFixture.getCaretOffset()));
    assertNotNull(info);
    assertEquals(AllIcons.RunConfigurations.TestState.Run_run, info.icon);
  }

  public void testFeatureRunLineContributorWhenFeatureHaveFailed() {
    PsiFile file = myFixture.configureByText("test.feature", "Feature: My feature");
    TestStateStorage stateStorage = TestStateStorage.getInstance(getProject());
    String testUrl = file.getVirtualFile().getUrl() + ":1";
    stateStorage.writeState(testUrl, new TestStateStorage.Record(TestStateInfo.Magnitude.FAILED_INDEX.getValue(), new Date(), 0, 0, "",
                                                                 "", ""));
    RunLineMarkerContributor.Info info = new CucumberRunLineMarkerContributor().getInfo(file.findElementAt(myFixture.getCaretOffset()));
    assertNotNull(info);
    assertEquals(AllIcons.RunConfigurations.TestState.Red2, info.icon);
  }

  public void testFeatureRunLineContributorWhenFeatureHaveSucceeded() {
    PsiFile file = myFixture.configureByText("test.feature", "Feature: My feature");
    TestStateStorage stateStorage = TestStateStorage.getInstance(getProject());
    String testUrl = file.getVirtualFile().getUrl() + ":1";
    stateStorage.writeState(testUrl, new TestStateStorage.Record(TestStateInfo.Magnitude.PASSED_INDEX.getValue(), new Date(), 0, 0, "",
                                                                 "", ""));
    RunLineMarkerContributor.Info info = new CucumberRunLineMarkerContributor().getInfo(file.findElementAt(myFixture.getCaretOffset()));
    assertNotNull(info);
    assertEquals(AllIcons.RunConfigurations.TestState.Green2, info.icon);
  }
}
