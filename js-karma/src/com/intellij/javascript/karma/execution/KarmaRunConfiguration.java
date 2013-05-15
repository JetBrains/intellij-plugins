package com.intellij.javascript.karma.execution;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author Sergey Simonchik
 */
public class KarmaRunConfiguration extends RunConfigurationBase implements LocatableConfiguration, RefactoringListenerProvider {

  private KarmaRunSettings myRunSettings = new KarmaRunSettings.Builder().build();
  private final ThreadLocal<GlobalSettings> myGlobalSettingsRef = new ThreadLocal<GlobalSettings>();

  protected KarmaRunConfiguration(@NotNull Project project, @NotNull ConfigurationFactory factory, @NotNull String name) {
    super(project, factory, name);
  }

  @Override
  public KarmaRunConfigurationEditor getConfigurationEditor() {
    return new KarmaRunConfigurationEditor(getProject());
  }

  @SuppressWarnings("deprecation")
  @Nullable
  @Override
  public JDOMExternalizable createRunnerSettings(ConfigurationInfoProvider provider) {
    return null;
  }

  @SuppressWarnings("deprecation")
  @Nullable
  @Override
  public SettingsEditor<JDOMExternalizable> getRunnerSettingsEditor(ProgramRunner runner) {
    return null;
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
    catch (RuntimeConfigurationException e) {
      // does nothing
    }
    GlobalSettings globalSettings = myGlobalSettingsRef.get();
    if (globalSettings == null) {
      return null;
    }
    return new KarmaTestRunnerState(getProject(),
                                    env,
                                    globalSettings.myNodeInterpreterPath,
                                    globalSettings.myKarmaNodePackage,
                                    myRunSettings);
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    myGlobalSettingsRef.remove();
    String nodeInterpreterPath = KarmaGlobalSettingsUtil.getNodeInterpreterPath();
    String karmaPackagePath = KarmaGlobalSettingsUtil.getKarmaNodePackageDir(getProject());
    check(nodeInterpreterPath, karmaPackagePath);
    if (nodeInterpreterPath != null && karmaPackagePath != null) {
      myGlobalSettingsRef.set(new GlobalSettings(nodeInterpreterPath, karmaPackagePath));
    }
  }

  private void check(@Nullable String nodeInterpreterPath, @Nullable String karmaPackagePath) throws RuntimeConfigurationError {
    if (nodeInterpreterPath == null || nodeInterpreterPath.trim().isEmpty()) {
      throw new RuntimeConfigurationError("Please specify Node.js interpreter.");
    }
    File nodeInterpreter = new File(nodeInterpreterPath);
    if (!nodeInterpreter.isFile() || !nodeInterpreter.canExecute() || !nodeInterpreter.isAbsolute()) {
      throw new RuntimeConfigurationError("Incorrect Node.js interpreter path.");
    }

    if (karmaPackagePath == null || karmaPackagePath.trim().isEmpty()) {
      throw new RuntimeConfigurationError("Please specify Karma Node.js package.");
    }
    File karmaPackageDir = new File(karmaPackagePath);
    if (!karmaPackageDir.isDirectory() || !karmaPackageDir.isAbsolute()) {
      throw new RuntimeConfigurationError("Incorrect Karma Node.js package.");
    }

    String configPath = myRunSettings.getConfigPath();
    if (configPath.trim().isEmpty()) {
      throw new RuntimeConfigurationError("Config file path is empty.");
    }
    File configFile = new File(configPath);
    if (!configFile.exists()) {
      throw new RuntimeConfigurationError("Configuration file does not exist.");
    }
    if (!configFile.isFile()) {
      throw new RuntimeConfigurationError("Specified config file path is incorrect.");
    }
  }

  @NotNull
  public KarmaRunSettings getRunSetting() {
    return myRunSettings;
  }

  public void setRunSettings(@NotNull KarmaRunSettings runSettings) {
    myRunSettings = runSettings;
  }

  @Override
  public boolean isGeneratedName() {
    String name = getName();
    if (name == null) {
      return false;
    }
    if ("Unnamed".equals(name)) {
      return true;
    }
    String prefix = "Unnamed (";
    String suffix = ")";
    if (name.startsWith(prefix) && name.endsWith(suffix)) {
      String id = name.substring(prefix.length(), name.length() - suffix.length());
      try {
        Integer.parseInt(id);
      } catch (Exception ignored) {}
      return true;
    }
    String suggestedName = suggestedName();
    return name.equals(suggestedName);
  }

  @Override
  public String suggestedName() {
    File file = new File(myRunSettings.getConfigPath());
    return file.getName();
  }

  @Nullable
  @Override
  public RefactoringElementListener getRefactoringElementListener(PsiElement element) {
    return null;
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
