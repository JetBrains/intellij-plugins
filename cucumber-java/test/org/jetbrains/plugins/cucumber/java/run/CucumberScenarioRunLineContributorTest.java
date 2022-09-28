// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.execution.TestStateStorage;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.execution.testframework.sm.runner.states.TestStateInfo;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.plugins.cucumber.java.CucumberJavaCodeInsightTestCase;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.cucumber.run.CucumberRunLineMarkerContributor;

import javax.swing.*;
import java.util.Date;

public class CucumberScenarioRunLineContributorTest extends CucumberJavaCodeInsightTestCase {
  private static final String myTestFeature = """
    Feature: My feature

      Scenario: test
        Given a cat""";

  public void testScenarioRunLineContributorWhenScenarioNeverHasBeenRan() {
    PsiFile file = myFixture.configureByText("test.feature", myTestFeature);
    PsiElement element = ((GherkinFile)file).getFeatures()[0].getScenarios()[0].findElementAt(0);
    checkInfo(element, AllIcons.RunConfigurations.TestState.Run_run);
  }

  public void testScenarioRunLineContributorWhenScenarioHaveFailed() {
    PsiFile file = myFixture.configureByText("test.feature", myTestFeature);
    TestStateStorage stateStorage = TestStateStorage.getInstance(getProject());
    String testUrl = file.getVirtualFile().getUrl() + ":3";
    stateStorage.writeState(testUrl, new TestStateStorage.Record(TestStateInfo.Magnitude.FAILED_INDEX.getValue(), new Date(), 0, 0, "",
                                                                 "", ""));
    PsiElement element = ((GherkinFile)file).getFeatures()[0].getScenarios()[0].findElementAt(0);
    checkInfo(element, AllIcons.RunConfigurations.TestState.Red2);
  }

  public void testScenarioRunLineContributorWhenScenarioHaveSucceeded() {
    PsiFile file = myFixture.configureByText("test.feature", myTestFeature);
    TestStateStorage stateStorage = TestStateStorage.getInstance(getProject());
    String testUrl = file.getVirtualFile().getUrl() + ":3";
    stateStorage.writeState(testUrl, new TestStateStorage.Record(TestStateInfo.Magnitude.PASSED_INDEX.getValue(), new Date(), 0, 0, "",
                                                                 "", ""));
    PsiElement element = ((GherkinFile)file).getFeatures()[0].getScenarios()[0].findElementAt(0);
    checkInfo(element, AllIcons.RunConfigurations.TestState.Green2);
  }
  
  private static void checkInfo(PsiElement element, Icon run) {
    RunLineMarkerContributor.Info info = new CucumberRunLineMarkerContributor().getInfo(element);
    assertNotNull(info);
    assertEquals(run, info.icon);
  }
}
