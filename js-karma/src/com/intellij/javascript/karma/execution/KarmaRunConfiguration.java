package com.intellij.javascript.karma.execution;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.javascript.karma.util.KarmaUtil;
import com.intellij.javascript.nodejs.NodePackageVersionUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.util.text.SemVer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class KarmaRunConfiguration extends LocatableConfigurationBase implements RefactoringListenerProvider {

  private KarmaRunSettings myRunSettings = new KarmaRunSettings.Builder().build();

  protected KarmaRunConfiguration(@NotNull Project project, @NotNull ConfigurationFactory factory, @NotNull String name) {
    super(project, factory, name);
  }

  @Override
  public RunConfiguration clone() {
    KarmaRunConfiguration clonedRc = (KarmaRunConfiguration)super.clone();
    clonedRc.initializeKarmaPackageDir();
    return clonedRc;
  }

  @NotNull
  public String getKarmaPackageDir() {
    String karmaPackageDir = myRunSettings.getKarmaPackageDir();
    if (StringUtil.isNotEmpty(karmaPackageDir)) {
      return karmaPackageDir;
    }
    return KarmaProjectSettings.getKarmaPackageDir(getProject());
  }

  private void initializeKarmaPackageDir() {
    if (StringUtil.isEmpty(myRunSettings.getKarmaPackageDir())) {
      Project project = getProject();
      KarmaProjectSettings projectSettings = KarmaProjectSettings.get(project);
      String karmaPackageDir = KarmaUtil.detectKarmaPackageDir(project,
                                                               myRunSettings.getConfigPath(),
                                                               projectSettings.getNodeInterpreterPath());
      if (StringUtil.isNotEmpty(karmaPackageDir)) {
        setKarmaPackageDir(karmaPackageDir, true);
      }
    }
  }

  @NotNull
  @Override
  public KarmaRunConfigurationEditor getConfigurationEditor() {
    return new KarmaRunConfigurationEditor(getProject());
  }

  @Override
  public void readExternal(Element element) throws InvalidDataException {
    super.readExternal(element);
    KarmaRunSettings runSettings = KarmaRunSettingsSerializationUtil.readXml(element);
    setRunSettings(runSettings);
  }

  @Override
  public void writeExternal(Element element) throws WriteExternalException {
    super.writeExternal(element);
    KarmaRunSettingsSerializationUtil.writeXml(element, myRunSettings);
  }

  @Nullable
  @Override
  public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
    String karmaPackageDir = getKarmaPackageDir();
    String nodeInterpreterPath = KarmaProjectSettings.getNodeInterpreterPath(getProject());
    return new KarmaRunProfileState(getProject(),
                                    env,
                                    nodeInterpreterPath,
                                    karmaPackageDir,
                                    myRunSettings,
                                    executor);
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
    myRunSettings = runSettings;
  }

  public void setConfigFilePath(@NotNull String configFilePath) {
    setRunSettings(new KarmaRunSettings.Builder(myRunSettings).setConfigPath(configFilePath).build());
  }

  public void setKarmaPackageDir(@NotNull String karmaPackageDir, boolean initializeOnly) {
    Project project = getProject();
    boolean local = FileUtil.toSystemIndependentName(karmaPackageDir).equals(FileUtil.toSystemIndependentName(myRunSettings.getKarmaPackageDir()));
    if (!local) {
      local = KarmaUtil.isPathUnderContentRoots(project, karmaPackageDir);
    }
    if (local) {
      if (StringUtil.isEmpty(myRunSettings.getKarmaPackageDir()) || !initializeOnly) {
        setRunSettings(new KarmaRunSettings.Builder(myRunSettings).setKarmaPackageDir(karmaPackageDir).build());
      }
    }
    else {
      String projectKarmaPackageDir = KarmaProjectSettings.getKarmaPackageDir(project);
      if (StringUtil.isEmpty(projectKarmaPackageDir) || !initializeOnly) {
        KarmaProjectSettings.setKarmaPackageDir(project, karmaPackageDir);
      }
    }
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
