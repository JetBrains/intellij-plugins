package com.intellij.javascript.karma.execution;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.javascript.karma.util.KarmaUtil;
import com.intellij.javascript.nodejs.NodePackageVersionUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.util.ObjectUtils;
import com.intellij.util.text.SemVer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class KarmaRunConfiguration extends LocatableConfigurationBase implements RefactoringListenerProvider {

  private static final Logger LOG = Logger.getInstance(KarmaRunConfiguration.class);

  private KarmaRunSettings myRunSettings = new KarmaRunSettings.Builder().build();

  protected KarmaRunConfiguration(@NotNull Project project, @NotNull ConfigurationFactory factory, @NotNull String name) {
    super(project, factory, name);
  }

  @NotNull
  @Override
  public KarmaRunConfigurationEditor getConfigurationEditor() {
    return new KarmaRunConfigurationEditor(getProject());
  }

  @Override
  public void readExternal(Element element) throws InvalidDataException {
    super.readExternal(element);
    myRunSettings = KarmaRunSettingsSerializationUtil.readXml(element);
  }

  @Override
  public void writeExternal(Element element) throws WriteExternalException {
    super.writeExternal(element);
    KarmaRunSettingsSerializationUtil.writeXml(element, myRunSettings);
  }

  @NotNull
  public String getKarmaPackageDir() {
    if (isTemplate()) {
      return StringUtil.notNullize(myRunSettings.getKarmaPackageDir());
    }
    String karmaPackageDir = getOrInitKarmaPackageDir();
    if (StringUtil.isNotEmpty(karmaPackageDir)) {
      return karmaPackageDir;
    }
    return KarmaProjectSettings.getKarmaPackageDir(getProject());
  }

  @NotNull
  private String getOrInitKarmaPackageDir() {
    if (myRunSettings.getKarmaPackageDir() == null) {
      Project project = getProject();
      KarmaProjectSettings projectSettings = KarmaProjectSettings.get(project);
      String karmaPackageDir = KarmaUtil.detectKarmaPackageDir(project,
                                                               myRunSettings.getConfigPath(),
                                                               projectSettings.getNodeInterpreterPath());
      karmaPackageDir = StringUtil.notNullize(karmaPackageDir);
      if (StringUtil.isNotEmpty(karmaPackageDir) && !KarmaUtil.isPathUnderContentRoots(project, karmaPackageDir)) {
        String projectKarmaPackageDir = KarmaProjectSettings.getKarmaPackageDir(project);
        if (StringUtil.isEmpty(projectKarmaPackageDir)) {
          KarmaProjectSettings.setKarmaPackageDir(project, karmaPackageDir);
        }
        karmaPackageDir = "";
      }
      myRunSettings = new KarmaRunSettings.Builder(myRunSettings).setKarmaPackageDir(karmaPackageDir).build();
    }
    return ObjectUtils.assertNotNull(myRunSettings.getKarmaPackageDir());
  }

  private boolean isTemplate() {
    return getTemplateRunConfiguration(getProject()) == this;
  }

  @Nullable
  private static KarmaRunConfiguration getTemplateRunConfiguration(@NotNull Project project) {
    RunManager runManager = RunManager.getInstance(project);
    RunnerAndConfigurationSettings templateSettings = runManager.getConfigurationTemplate(KarmaConfigurationType.getFactory());
    RunConfiguration rc = templateSettings.getConfiguration();
    if (rc instanceof KarmaRunConfiguration) {
      return (KarmaRunConfiguration)rc;
    }
    LOG.warn("No Karma template run configuration found: " + rc);
    return null;
  }

  @Nullable
  @Override
  public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
    String karmaPackageDir = getKarmaPackageDir();
    String nodeInterpreterPath = KarmaProjectSettings.getNodeInterpreterPath(getProject());
    return new KarmaRunProfileState(getProject(),
                                    this,
                                    env,
                                    nodeInterpreterPath,
                                    karmaPackageDir);
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    check(KarmaProjectSettings.getNodeInterpreterPath(getProject()), getKarmaPackageDir());
  }

  private void check(@NotNull String nodeInterpreterPath, @NotNull String karmaPackageDirPath) throws RuntimeConfigurationException {
    if (StringUtil.isEmpty(nodeInterpreterPath)) {
      throw new RuntimeConfigurationError("Please specify Node.js interpreter path");
    }
    File nodeInterpreter = new File(nodeInterpreterPath);
    if (!nodeInterpreter.isFile() || !nodeInterpreter.canExecute() || !nodeInterpreter.isAbsolute()) {
      throw new RuntimeConfigurationError("Please specify Node.js interpreter path correctly");
    }

    if (StringUtil.isEmpty(karmaPackageDirPath)) {
      throw new RuntimeConfigurationError("Please specify Karma package path");
    }
    File karmaPackageDir = new File(karmaPackageDirPath);
    if (!karmaPackageDir.isDirectory() || !karmaPackageDir.isAbsolute()) {
      throw new RuntimeConfigurationError("Please specify Karma package path correctly");
    }

    String configPath = myRunSettings.getConfigPath();
    if (configPath.trim().isEmpty()) {
      throw new RuntimeConfigurationError("Please specify config file path");
    }
    File configFile = new File(configPath);
    if (!configFile.exists()) {
      throw new RuntimeConfigurationError("Configuration file does not exist");
    }
    if (!configFile.isFile()) {
      throw new RuntimeConfigurationError("Please specify config file path correctly");
    }

    SemVer semVer = NodePackageVersionUtil.getPackageVersion(karmaPackageDir);
    if (semVer != null && semVer.getMajor() == 0 && semVer.getMinor() <= 8) {
      throw new RuntimeConfigurationWarning("Karma version 0.10 or higher is required. Specified karma version is " + semVer.getRawVersion());
    }
  }

  @NotNull
  public KarmaRunSettings getRunSettings() {
    return myRunSettings;
  }

  public void setRunSettings(@NotNull KarmaRunSettings runSettings) {
    String newKarmaPackageDir = StringUtil.notNullize(runSettings.getKarmaPackageDir());
    String oldKarmaPackageDir = FileUtil.toSystemDependentName(StringUtil.notNullize(myRunSettings.getKarmaPackageDir()));
    if (FileUtil.toSystemDependentName(newKarmaPackageDir).equals(oldKarmaPackageDir)) {
      myRunSettings = runSettings;
      return;
    }
    Project project = getProject();
    if (!KarmaUtil.isPathUnderContentRoots(project, newKarmaPackageDir)) {
      KarmaProjectSettings.setKarmaPackageDir(project, newKarmaPackageDir);
      newKarmaPackageDir = "";
    }
    myRunSettings = new KarmaRunSettings.Builder(runSettings).setKarmaPackageDir(newKarmaPackageDir).build();
  }

  public void setConfigFilePath(@NotNull String configFilePath) {
    myRunSettings = new KarmaRunSettings.Builder(myRunSettings).setConfigPath(configFilePath).build();
  }

  @Override
  public String suggestedName() {
    File file = new File(myRunSettings.getConfigPath());
    return file.getName();
  }

  @Nullable
  @Override
  public RefactoringElementListener getRefactoringElementListener(PsiElement element) {
    return KarmaRunConfigurationRefactoringHandler.getRefactoringElementListener(this, element);
  }
}
