/*
 * Copyright (C) 2020 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.thoughtworks.gauge.execution;

import com.intellij.execution.CommonProgramRunConfigurationParameters;
import com.intellij.execution.Executor;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.application.ApplicationConfigurationType;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizer;
import com.intellij.openapi.util.WriteExternalException;
import com.thoughtworks.gauge.GaugeConstants;
import com.thoughtworks.gauge.core.GaugeVersion;
import com.thoughtworks.gauge.settings.GaugeSettingsService;
import kotlin.text.StringsKt;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.thoughtworks.gauge.execution.GaugeDebugInfo.isDebugExecution;

public final class GaugeRunConfiguration extends LocatableConfigurationBase<GaugeRunConfiguration>
  implements RunProfileWithCompileBeforeLaunchOption {

  private static final Logger LOG = Logger.getInstance(GaugeRunConfiguration.class);

  public static final String TEST_RUNNER_SUPPORT_VERSION = "0.9.2";
  private String specsToExecute;
  private Module module;
  private String environment;
  private String tags;
  private boolean execInParallel;
  private String parallelNodes;
  public ApplicationConfiguration programParameters;
  private String rowsRange;
  private String moduleName;

  public GaugeRunConfiguration(String name, Project project, ConfigurationFactory configurationFactory) {
    super(project, configurationFactory, name);
    this.programParameters = new ApplicationConfiguration(name, project, ApplicationConfigurationType.getInstance());
  }

  @NotNull
  @Override
  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new GaugeExecutionConfigurationSettingsEditor();
  }

  @Override
  public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) {
    GeneralCommandLine commandLine = GaugeCommandLine.getInstance(getModule(), getProject());
    addFlags(commandLine, env);
    return new GaugeCommandLineState(commandLine, getProject(), env, this);
  }

  private void addFlags(GeneralCommandLine commandLine, ExecutionEnvironment env) {
    commandLine.addParameter(GaugeConstants.RUN);
    if (GaugeVersion.isGreaterOrEqual(TEST_RUNNER_SUPPORT_VERSION, true)
        && GaugeSettingsService.getSettings().useIntelliJTestRunner()) {
      LOG.info("Using IntelliJ Test Runner");
      commandLine.addParameter(GaugeConstants.MACHINE_READABLE);
      commandLine.addParameter(GaugeConstants.HIDE_SUGGESTION);
    }
    commandLine.addParameter(GaugeConstants.SIMPLE_CONSOLE);
    if (tags != null && !StringsKt.isBlank(tags)) {
      commandLine.addParameter(GaugeConstants.TAGS);
      commandLine.addParameter(tags);
    }
    if (environment != null && !StringsKt.isBlank(environment)) {
      commandLine.addParameters(GaugeConstants.ENV_FLAG, environment);
    }
    addTableRowsRangeFlags(commandLine);
    addParallelExecFlags(commandLine, env);
    addProgramArguments(commandLine);
    if (specsToExecute != null && !StringsKt.isBlank(specsToExecute)) {
      addSpecs(commandLine, specsToExecute);
    }
  }

  private void addTableRowsRangeFlags(GeneralCommandLine commandLine) {
    if (rowsRange != null && !StringsKt.isBlank(rowsRange)) {
      commandLine.addParameter(GaugeConstants.TABLE_ROWS);
      commandLine.addParameter(rowsRange);
    }
  }

  private void addProgramArguments(GeneralCommandLine commandLine) {
    if (programParameters == null) {
      return;
    }
    String parameters = programParameters.getProgramParameters();
    if (parameters != null && !parameters.isEmpty()) {
      commandLine.addParameters(programParameters.getProgramParameters().split(" "));
    }
    Map<String, String> envs = programParameters.getEnvs();
    if (!envs.isEmpty()) {
      commandLine.withEnvironment(envs);
    }
    String workingDirectory = programParameters.getWorkingDirectory();
    if (workingDirectory != null && !workingDirectory.isEmpty()) {
      commandLine.setWorkDirectory(new File(workingDirectory));
    }
  }

  private void addParallelExecFlags(GeneralCommandLine commandLine, ExecutionEnvironment env) {
    if (parallelExec(env)) {
      commandLine.addParameter(GaugeConstants.PARALLEL);
      try {
        if (parallelNodes != null && !parallelNodes.isEmpty()) {
          Integer.parseInt(this.parallelNodes);
          commandLine.addParameters(GaugeConstants.PARALLEL_NODES, parallelNodes);
        }
      }
      catch (NumberFormatException e) {
        LOG.warn("Incorrect number of parallel execution streams specified: " + parallelNodes);
      }
    }
  }

  private boolean parallelExec(ExecutionEnvironment env) {
    return execInParallel && !isDebugExecution(env);
  }

  private static void addSpecs(GeneralCommandLine commandLine, String specsToExecute) {
    String[] specNames = specsToExecute.split(GaugeConstants.SPEC_FILE_DELIMITER_REGEX);
    for (String specName : specNames) {
      if (!specName.isEmpty()) {
        commandLine.addParameter(specName.trim());
      }
    }
  }

  @Override
  public void readExternal(@NotNull Element element) throws InvalidDataException {
    super.readExternal(element);
    environment = JDOMExternalizer.readString(element, "environment");
    specsToExecute = JDOMExternalizer.readString(element, "specsToExecute");
    tags = JDOMExternalizer.readString(element, "tags");
    parallelNodes = JDOMExternalizer.readString(element, "parallelNodes");
    execInParallel = JDOMExternalizer.readBoolean(element, "execInParallel");
    programParameters.setProgramParameters(JDOMExternalizer.readString(element, "programParameters"));
    programParameters.setWorkingDirectory(JDOMExternalizer.readString(element, "workingDirectory"));
    this.moduleName = JDOMExternalizer.readString(element, "moduleName");
    HashMap<String, String> envMap = new HashMap<>();
    JDOMExternalizer.readMap(element, envMap, "envMap", "envMap");
    programParameters.setEnvs(envMap);
    rowsRange = JDOMExternalizer.readString(element, "rowsRange");
  }

  @Override
  public void writeExternal(@NotNull Element element) throws WriteExternalException {
    super.writeExternal(element);
    JDOMExternalizer.write(element, "environment", environment);
    JDOMExternalizer.write(element, "specsToExecute", specsToExecute);
    JDOMExternalizer.write(element, "tags", tags);
    JDOMExternalizer.write(element, "parallelNodes", parallelNodes);
    JDOMExternalizer.write(element, "execInParallel", execInParallel);
    JDOMExternalizer.write(element, "programParameters", programParameters.getProgramParameters());
    JDOMExternalizer.write(element, "workingDirectory", programParameters.getWorkingDirectory());
    JDOMExternalizer.write(element, "moduleName", moduleName);
    JDOMExternalizer.writeMap(element, programParameters.getEnvs(), "envMap", "envMap");
    JDOMExternalizer.write(element, "rowsRange", rowsRange);
  }

  @Override
  public Module @NotNull [] getModules() {
    return ModuleManager.getInstance(getProject()).getModules();
  }

  public void setSpecsToExecute(String specsToExecute) {
    this.specsToExecute = specsToExecute;
  }

  public String getSpecsToExecute() {
    return specsToExecute;
  }

  public void setModule(Module module) {
    this.module = module;
    this.moduleName = module.getName();
  }

  public Module getModule() {
    if (module == null) {
      return ModuleManager.getInstance(getProject()).findModuleByName(this.moduleName);
    }
    return module;
  }

  public String getEnvironment() {
    return environment;
  }

  public void setEnvironment(String environment) {
    this.environment = environment;
  }

  public String getTags() {
    return tags;
  }

  public void setTags(String tags) {
    this.tags = tags;
  }

  public void setSpecsArrayToExecute(List<String> specsArrayToExecute) {
    StringBuilder builder = new StringBuilder();
    for (String specName : specsArrayToExecute) {
      builder.append(specName);
      if (specsArrayToExecute.indexOf(specName) != specsArrayToExecute.size() - 1) {
        builder.append(GaugeConstants.SPEC_FILE_DELIMITER);
      }
    }
    setSpecsToExecute(builder.toString());
  }

  public void setExecInParallel(boolean execInParallel) {
    this.execInParallel = execInParallel;
  }

  public boolean getExecInParallel() {
    return execInParallel;
  }

  public void setParallelNodes(String parallelNodes) {
    this.parallelNodes = parallelNodes;
  }

  public String getParallelNodes() {
    return parallelNodes;
  }

  public CommonProgramRunConfigurationParameters getProgramParameters() {
    return programParameters;
  }

  public String getRowsRange() {
    return rowsRange;
  }

  public void setRowsRange(String rowsRange) {
    this.rowsRange = rowsRange;
  }
}
