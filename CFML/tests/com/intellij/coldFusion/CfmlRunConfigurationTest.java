package com.intellij.coldFusion;

import com.intellij.coldFusion.UI.runner.CfmlRunConfiguration;
import com.intellij.coldFusion.UI.runner.CfmlRunConfigurationType;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunManagerEx;
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
    TestCase.assertEquals(true, StringUtil.startsWith(url, "http://localhost:8500/"));
    TestCase.assertEquals(false, StringUtil.startsWith(url, "http://localhost:8500//"));
  }

  public CfmlRunConfiguration getDefaultCfmlRunConfiguration() {
    final Project project = myFixture.getProject();
    final Editor editor = openCfmFileInEditor(indexCfm);
    final Presentation presentation = getPresentationForRunAction(editor);
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

    TestCase.assertEquals(true, configs.size() == 1);
    final ConfigurationFromContext defaultConfigurationFromContext = configs.get(0);
    final RunConfiguration configuration = defaultConfigurationFromContext.getConfiguration();
    TestCase.assertNotNull(configuration);
    UsefulTestCase.assertInstanceOf(configuration, CfmlRunConfiguration.class);
    return (CfmlRunConfiguration)configuration;
  }

  public void generateNonDefaultRunConfiguration() {
    final CfmlRunConfiguration defaultCfmlRunConfiguration = getDefaultCfmlRunConfiguration();
    final CfmlRunConfiguration clonedConfiguration = (CfmlRunConfiguration) defaultCfmlRunConfiguration.clone();
    clonedConfiguration.getRunnerParameters().setUrl("http://4.4.4.4/src/index.cfm");
    final RunnerAndConfigurationSettings runnerAndConfigurationSettings = RunManager.getInstance(getProject())
      .createConfiguration(clonedConfiguration, CfmlRunConfigurationType.getInstance().getConfigurationFactories()[0]);
    RunManager.getInstance(getProject()).addConfiguration(runnerAndConfigurationSettings, false);
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
      ((RunManagerEx)configurationContext.getRunManager()).setTemporaryConfiguration(configuration);
    }
    //end of the emulated block
    return (CfmlRunConfiguration)configuration.getConfiguration();
  }

  public void testNonDefaultRunConfiguration() throws IOException {
    generateNonDefaultRunConfiguration();
    final Editor editor = myFixture.getEditor();
    final CfmlRunConfiguration cfmlConfig = getContextRunConfiguration(editor);
    assert cfmlConfig != null;
    cfmlConfig.getRunnerParameters().getUrl();
    assertEquals("http://4.4.4.4/src/index.cfm", cfmlConfig.getRunnerParameters().getUrl());
  }


  public void testCreateContextRunConfiguration() throws IOException {
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

  public Presentation getPresentationForRunAction(Editor editor){
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
