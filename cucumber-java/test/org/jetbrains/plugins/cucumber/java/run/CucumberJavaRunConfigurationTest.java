// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.execution.Location;
import com.intellij.execution.PsiLocation;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.TestApplicationManager;
import com.intellij.testFramework.TestDataProvider;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.java.CucumberJavaCodeInsightTestCase;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

public class CucumberJavaRunConfigurationTest extends CucumberJavaCodeInsightTestCase {
  public void testScenarioOutlineNameFilter() {
    doTest("^a few cukes$");
  }

  public void testScenarioOutlineWithPatternNameFilter() {
    doTest("^a .* few cukes$");
  }

  public void testDoNotCreateRunConfigurationOnFoldersWithoutFeatureFiles() {
    doFolderTest(false);
  }

  public void testCreateRunConfigurationOnFoldersWithFeatureFiles() {
    doFolderTest(true);
  }

  public void testProgramArguments() {
    myFixture.configureByText("test.feature", "Fea<caret>ture: test");

    CucumberJavaRunConfiguration runConfiguration = createTemplateConfiguration();
    runConfiguration.setProgramParameters("--plugin pretty");
    CucumberJavaFeatureRunConfigurationProducer producer = new CucumberJavaFeatureRunConfigurationProducer();
    ConfigurationContext configurationContext = getConfigurationContext();

    PsiElement elementAtCaret = myFixture.getFile().findElementAt(myFixture.getCaretOffset());
    producer.setupConfigurationFromContext(runConfiguration, configurationContext, new Ref<>(elementAtCaret));

    String parameters = runConfiguration.getProgramParameters();
    assertNotNull(parameters);
    assertTrue(parameters.contains("--plugin pretty"));
    assertFalse(parameters.contains("org.jetbrains.plugins.cucumber.java.run.CucumberJvm"));
  }

  @Override
  protected String getBasePath() {
    return CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "run";
  }

  private void doTest(@Nullable String expectedFilter) {
    myFixture.copyDirectoryToProject(getTestName(true), "");
    myFixture.configureByFile("test.feature");

    CucumberJavaRunConfiguration runConfiguration = createTemplateConfiguration();
    ConfigurationContext configurationContext = getConfigurationContext();

    CucumberJavaScenarioRunConfigurationProducer producer = new CucumberJavaScenarioRunConfigurationProducer();
    PsiElement elementAtCaret = myFixture.getFile().findElementAt(myFixture.getCaretOffset());
    assertTrue(producer.setupConfigurationFromContext(runConfiguration, configurationContext, new Ref<>(elementAtCaret)));

    assertEquals(expectedFilter, runConfiguration.getNameFilter());
  }

  private void doFolderTest(boolean isRunConfigurationExpected) {
    myFixture.copyDirectoryToProject(getTestName(true), "");
    myFixture.configureByFile("StepDefs.java");

    CucumberJavaRunConfiguration runConfiguration = createTemplateConfiguration();

    PsiDirectory psiDirectory = myFixture.getFile().getParent();
    Location location = new PsiLocation<>(psiDirectory);
    ConfigurationContext configurationContext = ConfigurationContext.createEmptyContextForLocation(location);

    CucumberJavaAllFeaturesInFolderRunConfigurationProducer producer = new CucumberJavaAllFeaturesInFolderRunConfigurationProducer();
    assertEquals(isRunConfigurationExpected, producer.setupConfigurationFromContext(runConfiguration, configurationContext, new Ref<>(psiDirectory)));
  }

  @NotNull
  private CucumberJavaRunConfiguration createTemplateConfiguration() {
    ConfigurationFactory configurationFactory = CucumberJavaRunConfigurationType.getInstance().getConfigurationFactories()[0];
    return new CucumberJavaRunConfiguration("", myFixture.getProject(), configurationFactory);
  }

  @NotNull
  private ConfigurationContext getConfigurationContext() {
    DataContext dataContext = DataManager.getInstance().getDataContext(myFixture.getEditor().getComponent());
    return ConfigurationContext.getFromContext(dataContext, ActionPlaces.UNKNOWN);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    TestApplicationManager.getInstance().setDataProvider(new TestDataProvider(getProject()) {
      @Override
      public Object getData(@NotNull @NonNls String dataId) {
        if (LangDataKeys.MODULE.is(dataId)) {
          return myFixture.getModule();
        }
        return super.getData(dataId);
      }
    });
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumber2ProjectDescriptor();
  }
}
