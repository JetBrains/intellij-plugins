// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime;

import com.intellij.execution.CommonProgramRunConfigurationParameters;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.ExternalizablePath;
import com.intellij.execution.configuration.EnvironmentVariablesComponent;
import com.intellij.execution.configurations.*;
import com.intellij.execution.process.KillableColoredProcessHandler;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.util.ProgramParametersUtil;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xmlb.XmlSerializer;
import org.intellij.terraform.config.util.TFExecutor;
import org.intellij.terraform.hcl.HCLBundle;
import org.jdom.Element;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public final class TerraformRunConfiguration extends LocatableConfigurationBase implements CommonProgramRunConfigurationParameters {
  public String PROGRAM_PARAMETERS = "";
  public String WORKING_DIRECTORY = "";
  private final Map<String, String> myEnvs = new LinkedHashMap<>();
  public boolean PASS_PARENT_ENVS = true;

  public TerraformRunConfiguration(final Project project, final ConfigurationFactory factory, final String name) {
    super(project, factory, name);
  }

  @NotNull
  @Override
  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new TerraformRunConfigurationEditor();
  }

  @Override
  public RunProfileState getState(@NotNull final Executor executor, @NotNull final ExecutionEnvironment env) throws ExecutionException {
    final String error = getError();
    if (error != null) {
      throw new ExecutionException(error);
    }

    return new CommandLineState(env) {
      @NotNull
      @Override
      protected ProcessHandler startProcess() throws ExecutionException {
        OSProcessHandler handler;
        handler = new KillableColoredProcessHandler(createCommandLine());
        ProcessTerminatedListener.attach(handler);
        return handler;
      }

      private GeneralCommandLine createCommandLine() throws ExecutionException {
        final SimpleProgramParameters parameters = getParameters();

        return TFExecutor.in(getProject(), null)
            .withPresentableName(HCLBundle.message("terraform.run.configuration.name"))
            .withWorkDirectory(parameters.getWorkingDirectory())
            .withParameters(parameters.getProgramParametersList().getParameters())
            .withPassParentEnvironment(parameters.isPassParentEnvs())
            .withExtraEnvironment(parameters.getEnv())
            .showOutputOnError()
            .createCommandLine();
      }

      private SimpleProgramParameters getParameters() {
        final SimpleProgramParameters params = new SimpleProgramParameters();

        ProgramParametersUtil.configureConfiguration(params, TerraformRunConfiguration.this);

        return params;
      }
    };
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    if (StringUtil.isEmptyOrSpaces(WORKING_DIRECTORY)) {
      RuntimeConfigurationException exception = new RuntimeConfigurationException(HCLBundle.message("run.configuration.no.working.directory.specified"));
      exception.setQuickFix(() -> setWorkingDirectory(getProject().getBasePath()));
      throw exception;
    }

    final String error = getError();
    if (error != null) {
      throw new RuntimeConfigurationException(error);
    }
  }

  @Nls
  private String getError() {
    if (StringUtil.isEmptyOrSpaces(WORKING_DIRECTORY)) {
      return (HCLBundle.message("run.configuration.no.working.directory.specified"));
    }
    final String terraformPath = TerraformToolProjectSettings.getInstance(getProject()).getTerraformPath();
    if (StringUtil.isEmptyOrSpaces(terraformPath)) {
      return (HCLBundle.message("run.configuration.no.terraform.specified"));
    }
    if (!FileUtil.canExecute(new File(terraformPath))) {
      return (HCLBundle.message("run.configuration.terraform.path.incorrect"));
    }
    return null;
  }

  @Override
  public void setProgramParameters(String value) {
    PROGRAM_PARAMETERS = value;
  }

  @Override
  public String getProgramParameters() {
    return PROGRAM_PARAMETERS;
  }

  @Override
  public void setWorkingDirectory(String value) {
    WORKING_DIRECTORY = ExternalizablePath.urlValue(value);
  }

  @Override
  public String getWorkingDirectory() {
    return ExternalizablePath.localPathValue(WORKING_DIRECTORY);
  }

  @Override
  public void setPassParentEnvs(boolean passParentEnvs) {
    PASS_PARENT_ENVS = passParentEnvs;
  }

  @Override
  @NotNull
  public Map<String, String> getEnvs() {
    return myEnvs;
  }

  @Override
  public void setEnvs(@NotNull final Map<String, String> envs) {
    myEnvs.clear();
    myEnvs.putAll(envs);
  }

  @Override
  public boolean isPassParentEnvs() {
    return PASS_PARENT_ENVS;
  }

  @Override
  public void readExternal(@NotNull final Element element) throws InvalidDataException {
    super.readExternal(element);
    XmlSerializer.deserializeInto(this, element);
    EnvironmentVariablesComponent.readExternal(element, getEnvs());
  }

  @Override
  public void writeExternal(@NotNull Element element) throws WriteExternalException {
    super.writeExternal(element);
    XmlSerializer.serializeInto(this, element);
    EnvironmentVariablesComponent.writeExternal(element, getEnvs());
  }
}
