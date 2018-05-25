// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.intellij.coldFusion;

import com.intellij.coldFusion.UI.runner.CfmlRunConfiguration;
import com.intellij.coldFusion.UI.runner.CfmlRunConfigurationType;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.ConfigurationFromContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.lineMarker.ExecutorAction;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.UsefulTestCase;
import junit.framework.TestCase;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class CfmlRunConfigurationTest extends CfmlCodeInsightFixtureTestCase {
  private static final String WWWROOT_SRC = "wwwroot/src/";
  private PsiFile indexCfm;
  private PsiFile index2Cfm;

  protected void setUp() throws Exception {
    super.setUp();
    indexCfm = createCfmPsiFile("index.cfm");
    index2Cfm = createCfmPsiFile("index2.cfm");
  }

  public void testDefaultRunConfiguration() {
    final CfmlRunConfiguration configuration = getDefaultCfmlRunConfiguration();
    final String url = configuration.getRunnerParameters().getUrl();
    assertThat(url).startsWith("http://localhost:8500/");
    assertThat(url).doesNotStartWith("http://localhost:8500//");
  }

  public CfmlRunConfiguration getDefaultCfmlRunConfiguration() {
    final Project project = myFixture.getProject();
    final Editor editor = openCfmFileInEditor(indexCfm);
    final Presentation presentation = getPresentationForRunAction();
    assert editor != null;
    final DataContext dataContext = DataManager.getInstance().getDataContext(editor.getComponent());


    TestCase.assertEquals("Run 'index.cfm'", presentation.getText());
    final ConfigurationContext configurationContext = ConfigurationContext.getFromContext(dataContext);
    final List<RunConfigurationProducer<?>> producers = RunConfigurationProducer.getProducers(project);
    List<ConfigurationFromContext> configs = new ArrayList<>();
    for (RunConfigurationProducer<?> producer : producers) {
      final ConfigurationFromContext configurationFromContext = producer.createConfigurationFromContext(configurationContext);
      if (configurationFromContext != null) configs.add(configurationFromContext);
    }
    List<ConfigurationFromContext> cfmlRunContextConfigurations =
      configs.stream().filter(context -> context.getConfiguration() instanceof CfmlRunConfiguration).collect(Collectors.toList());
    assertThat(cfmlRunContextConfigurations).hasSize(1);
    final RunConfiguration configuration = cfmlRunContextConfigurations.get(0).getConfiguration();
    TestCase.assertNotNull(configuration);
    UsefulTestCase.assertInstanceOf(configuration, CfmlRunConfiguration.class);
    return (CfmlRunConfiguration)configuration;
  }

  public void generateNonDefaultRunConfiguration() {
    final CfmlRunConfiguration defaultCfmlRunConfiguration = getDefaultCfmlRunConfiguration();
    final CfmlRunConfiguration clonedConfiguration = (CfmlRunConfiguration) defaultCfmlRunConfiguration.clone();
    clonedConfiguration.getRunnerParameters().setUrl("http://4.4.4.4/src/index.cfm");
    RunManager runManager = RunManager.getInstance(getProject());
    RunnerAndConfigurationSettings runnerAndConfigurationSettings = runManager.createConfiguration(clonedConfiguration, CfmlRunConfigurationType.getInstance().getConfigurationFactories()[0]);
    runManager.addConfiguration(runnerAndConfigurationSettings);
  }

  private static CfmlRunConfiguration getContextRunConfiguration(Editor editor){
    final DataContext dataContext = DataManager.getInstance().getDataContext(editor.getComponent());
    final ConfigurationContext configurationContext = ConfigurationContext.getFromContext(dataContext);

    //this block emulates RunContextAction.perform()
    RunnerAndConfigurationSettings configuration = configurationContext.findExisting();
    if (configuration == null) {
      configuration = configurationContext.getConfiguration();
      if (configuration == null) {
        return null;
      }
      configurationContext.getRunManager().setTemporaryConfiguration(configuration);
    }
    //end of the emulated block
    return (CfmlRunConfiguration)configuration.getConfiguration();
  }

  public void testNonDefaultRunConfiguration() {
    generateNonDefaultRunConfiguration();
    final Editor editor = myFixture.getEditor();
    final CfmlRunConfiguration cfmlConfig = getContextRunConfiguration(editor);
    assert cfmlConfig != null;
    cfmlConfig.getRunnerParameters().getUrl();
    assertEquals("http://4.4.4.4/src/index.cfm", cfmlConfig.getRunnerParameters().getUrl());
  }


  public void testCreateContextRunConfiguration() {
    generateNonDefaultRunConfiguration();
    final Editor editor = openCfmFileInEditor(index2Cfm);
    final CfmlRunConfiguration configuration = getContextRunConfiguration(editor);
    assert configuration != null;
    TestCase.assertEquals("http://localhost:8500/src/index2.cfm", configuration.getRunnerParameters().getUrl());
    TestCase.assertEquals("index2.cfm", configuration.getName());
  }

  public PsiFile createCfmPsiFile(String filename) throws IOException {
    String filePath = getDataPath() + filename;
    FileInputStream fileInputStream = new FileInputStream(new File(filePath));
    try {
      String testText = StringUtil.convertLineSeparators(FileUtil.loadTextAndClose(fileInputStream));
      return myFixture.addFileToProject(WWWROOT_SRC + filename, testText);
    } catch (FileNotFoundException fnfe) {
      fnfe.printStackTrace();
    } finally {
      fileInputStream.close();
    }
    return null;
  }

  @Nullable
  public Editor openCfmFileInEditor(PsiFile cfmFile) {
    myFixture.openFileInEditor(cfmFile.getVirtualFile());
    final Editor editor = myFixture.getEditor();
    myFixture.openFileInEditor(cfmFile.getVirtualFile());
    return editor;
  }

  public Presentation getPresentationForRunAction() {
    AnAction[] actions = ExecutorAction.getActions();
    final AnAction runAction = actions[0];
    AnActionEvent e = AnActionEvent.createFromDataContext(ActionPlaces.EDITOR_POPUP, null, DataManager
      .getInstance().getDataContext(myFixture.getEditor().getComponent()));
    runAction.update(e);
    return e.getPresentation();
  }

  protected String getDataPath() {
    return CfmlTestUtil.BASE_TEST_DATA_PATH + "/runconfig/";
  }
}
