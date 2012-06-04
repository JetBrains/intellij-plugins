/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.jstestdriver.idea.execution;

import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.execution.settings.JstdRunSettingsSerializationUtils;
import com.google.jstestdriver.idea.execution.settings.TestType;
import com.google.jstestdriver.idea.execution.settings.ui.JstdRunConfigurationEditor;
import com.google.jstestdriver.idea.util.ProjectRootUtils;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * One configured instance of the Run Configuration. The user can create several different configs
 * and save them all.
 *
 * @author alexeagle@google.com (Alex Eagle)
 */
public class JstdRunConfiguration extends RunConfigurationBase implements LocatableConfiguration, RefactoringListenerProvider {

  private @NotNull JstdRunSettings myRunSettings = new JstdRunSettings.Builder().build();
  private volatile String myGeneratedName;

  public JstdRunConfiguration(Project project,
                              ConfigurationFactory jsTestDriverConfigurationFactory,
                              String pluginName) {
    super(project, jsTestDriverConfigurationFactory, pluginName);
  }

  @Override
  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new JstdRunConfigurationEditor(getProject());
  }

  @SuppressWarnings({"deprecation", "UnnecessaryFullyQualifiedName"})
  @Override
  public com.intellij.openapi.util.JDOMExternalizable createRunnerSettings(ConfigurationInfoProvider provider) {
    return null;
  }

  @SuppressWarnings({"deprecation", "UnnecessaryFullyQualifiedName"})
  @Override
  public SettingsEditor<com.intellij.openapi.util.JDOMExternalizable> getRunnerSettingsEditor(ProgramRunner runner) {
    return null;
  }

  @Override
  public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env)
    throws ExecutionException {
    if (DefaultDebugExecutor.EXECUTOR_ID.equals(executor.getId())) {
      throw new ExecutionException("Debugging is not implemented");
    }
    return getCoverageState(env, null);
  }

  public RunProfileState getCoverageState(@NotNull ExecutionEnvironment env, @Nullable String coverageFilePath)
    throws ExecutionException {
    try {
      checkConfiguration();
    }
    catch (RuntimeConfigurationException e) {
      throw new ExecutionException(e.getMessage());
    }
    return new JstdTestRunnerCommandLineState(getProject(), env, myRunSettings, coverageFilePath);
  }

  public void setRunSettings(@NotNull JstdRunSettings runSettings) {
    myRunSettings = runSettings;
  }

  @NotNull
  public JstdRunSettings getRunSettings() {
    return myRunSettings;
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    JstdRunConfigurationVerifier.verify(myRunSettings);
  }

  @Override
  public void readExternal(Element element) throws InvalidDataException {
    super.readExternal(element);
    JstdRunSettings runSettings = JstdRunSettingsSerializationUtils.readFromXml(element);
    setRunSettings(runSettings);
  }

  @Override
  public void writeExternal(Element element) throws WriteExternalException {
    super.writeExternal(element);
    JstdRunSettingsSerializationUtils.writeToXml(element, myRunSettings);
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
  @NotNull
  public String suggestedName() {
    String generatedName = myGeneratedName;
    if (myGeneratedName == null) {
      generatedName = generateName();
      myGeneratedName = generatedName;
    }
    return generatedName;
  }

  @NotNull
  private String generateName() {
    TestType testType = myRunSettings.getTestType();
    if (testType == TestType.ALL_CONFIGS_IN_DIRECTORY) {
      String directoryPath = myRunSettings.getDirectory();
      String rootRelativePath = ProjectRootUtils.getRootRelativePath(getProject(), directoryPath);
      if (rootRelativePath == null) {
        rootRelativePath = directoryPath;
      }
      return "All in " + rootRelativePath;
    }
    else if (testType == TestType.CONFIG_FILE) {
      File file = new File(myRunSettings.getConfigFile());
      return file.getName();
    }
    else if (testType == TestType.JS_FILE) {
      File file = new File(myRunSettings.getJsFilePath());
      return file.getName();
    }
    else if (testType == TestType.TEST_CASE) {
      return myRunSettings.getTestCaseName();
    }
    else if (testType == TestType.TEST_METHOD) {
      return myRunSettings.getTestCaseName() + "." + myRunSettings.getTestMethodName();
    }
    return "Unnamed";
  }

  @Override
  @Nullable
  public RefactoringElementListener getRefactoringElementListener(PsiElement element) {
    JstdRunConfigurationRefactoringHandler refactoringHandler = new JstdRunConfigurationRefactoringHandler(this);
    return refactoringHandler.getRefactoringElementListener(element);
  }
}
