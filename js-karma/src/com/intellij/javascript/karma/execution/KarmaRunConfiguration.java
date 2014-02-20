package com.intellij.javascript.karma.execution;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.util.text.SemVer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author Sergey Simonchik
 */
public class KarmaRunConfiguration extends LocatableConfigurationBase implements RefactoringListenerProvider {

  private KarmaRunSettings myRunSettings = new KarmaRunSettings.Builder().build();
  private final ThreadLocal<GlobalSettings> myGlobalSettingsRef = new ThreadLocal<GlobalSettings>();

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
    KarmaRunSettings runSettings = KarmaRunSettingsSerializationUtil.readFromXml(element);
    setRunSettings(runSettings);
  }

  @Override
  public void writeExternal(Element element) throws WriteExternalException {
    super.writeExternal(element);
    KarmaRunSettingsSerializationUtil.writeToXml(element, myRunSettings);
  }

  @Nullable
  @Override
  public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
    try {
      checkConfiguration();
    }
    catch (RuntimeConfigurationError e) {
      throw new ExecutionException(e.getMessage());
    }
    catch (RuntimeConfigurationException ignored) {
      // does nothing
    }
    GlobalSettings globalSettings = myGlobalSettingsRef.get();
    if (globalSettings == null) {
      return null;
    }
    return new KarmaRunProfileState(getProject(),
                                    env,
                                    globalSettings.myNodeInterpreterPath,
                                    globalSettings.myKarmaNodePackage,
                                    myRunSettings,
                                    executor);
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    myGlobalSettingsRef.remove();
    String nodeInterpreterPath = KarmaGlobalSettingsUtil.getNodeInterpreterPath();
    String karmaPackagePath = KarmaGlobalSettingsUtil.getKarmaNodePackageDir(getProject(), myRunSettings.getConfigPath());
    boolean ok = true;
    try {
      check(nodeInterpreterPath, karmaPackagePath);
    }
    catch (RuntimeConfigurationError e) {
      ok = false;
      throw e;
    }
    finally {
      if (ok && nodeInterpreterPath != null && karmaPackagePath != null) {
        myGlobalSettingsRef.set(new GlobalSettings(nodeInterpreterPath, karmaPackagePath));
      }
    }
  }

  private void check(@Nullable String nodeInterpreterPath, @Nullable String karmaPackagePath) throws RuntimeConfigurationException {
    if (nodeInterpreterPath == null || nodeInterpreterPath.trim().isEmpty()) {
      throw new RuntimeConfigurationError("Please specify Node.js interpreter path");
    }
    File nodeInterpreter = new File(nodeInterpreterPath);
    if (!nodeInterpreter.isFile() || !nodeInterpreter.canExecute() || !nodeInterpreter.isAbsolute()) {
      throw new RuntimeConfigurationError("Please specify Node.js interpreter path correctly");
    }

    if (karmaPackagePath == null || karmaPackagePath.trim().isEmpty()) {
      throw new RuntimeConfigurationError("Please specify Karma package path");
    }
    File karmaPackageDir = new File(karmaPackagePath);
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

  private static class GlobalSettings {
    private final String myNodeInterpreterPath;
    private final String myKarmaNodePackage;

    private GlobalSettings(@NotNull String nodeInterpreterPath, @NotNull String karmaNodePackage) {
      myKarmaNodePackage = karmaNodePackage;
      myNodeInterpreterPath = nodeInterpreterPath;
    }
  }
}
