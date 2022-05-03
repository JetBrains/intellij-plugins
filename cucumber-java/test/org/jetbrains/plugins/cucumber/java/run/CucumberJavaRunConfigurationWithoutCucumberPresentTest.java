// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.execution.PsiLocation;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.openapi.util.Ref;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberCodeInsightTestCase;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

public class CucumberJavaRunConfigurationWithoutCucumberPresentTest extends CucumberCodeInsightTestCase {

  public void testDoNotCreateRunConfigurationWithoutCucumberLibrariesPresentInProject() {
    myFixture.copyDirectoryToProject(getTestName(true), "");
    myFixture.configureByFile("StepDefs.java");

    var runConfiguration = createTemplateConfiguration();
    var psiDirectory = myFixture.getFile().getParent();
    var location = new PsiLocation<>(psiDirectory);
    var configurationContext = ConfigurationContext.createEmptyContextForLocation(location);
    var producer = new CucumberJavaAllFeaturesInFolderRunConfigurationProducer();

    assertFalse("CucumberJavaRunConfiguration was not supposed to be produced, but it did!",
                producer.setupConfigurationFromContext(runConfiguration, configurationContext, new Ref<>(psiDirectory)));
  }

  @Override
  protected String getBasePath() {
    return CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "run";
  }

  @NotNull
  private CucumberJavaRunConfiguration createTemplateConfiguration() {
    var configurationFactory = CucumberJavaRunConfigurationType.getInstance().getConfigurationFactories()[0];
    return new CucumberJavaRunConfiguration("", myFixture.getProject(), configurationFactory);
  }
}
