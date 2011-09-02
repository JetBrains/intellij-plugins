package com.intellij.lang.javascript.flex.run;

import com.intellij.execution.CantRunException;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.options.SdkEntry;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.PathUtil;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class FlexIdeRunConfiguration extends RunConfigurationBase implements RunProfileWithCompileBeforeLaunchOption {

  private FlexIdeRunnerParameters myRunnerParameters = new FlexIdeRunnerParameters();

  public FlexIdeRunConfiguration(final Project project, final ConfigurationFactory factory, final String name) {
    super(project, factory, name);
  }

  public FlexIdeRunConfiguration clone() {
    final FlexIdeRunConfiguration clone = (FlexIdeRunConfiguration)super.clone();
    clone.myRunnerParameters = myRunnerParameters.clone();
    return clone;
  }

  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new FlexIdeRunConfigurationForm(getProject());
  }

  public JDOMExternalizable createRunnerSettings(final ConfigurationInfoProvider provider) {
    return null;
  }

  public SettingsEditor<JDOMExternalizable> getRunnerSettingsEditor(final ProgramRunner runner) {
    return null;
  }

  @Override
  public void readExternal(final Element element) throws InvalidDataException {
    super.readExternal(element);
    myRunnerParameters = new FlexIdeRunnerParameters();
    XmlSerializer.deserializeInto(myRunnerParameters, element);
  }

  @Override
  public void writeExternal(final Element element) throws WriteExternalException {
    super.writeExternal(element);
    XmlSerializer.serializeInto(myRunnerParameters, element);
  }

  public RunProfileState getState(@NotNull final Executor executor, @NotNull final ExecutionEnvironment env) throws ExecutionException {
    final FlexIdeBuildConfiguration config = FlexBaseRunner.getModuleAndConfig(getProject(), myRunnerParameters).second;

    if (config.TARGET_PLATFORM == FlexIdeBuildConfiguration.TargetPlatform.Desktop) {
      final AirRunState airRunState = new AirRunState(env);
      airRunState.setConsoleBuilder(TextConsoleBuilderFactory.getInstance().createBuilder(getProject()));
      return airRunState;
    }

    return FlexBaseRunner.EMPTY_RUN_STATE;
  }

  public void checkConfiguration() throws RuntimeConfigurationException {
  }

  @NotNull
  public FlexIdeRunnerParameters getRunnerParameters() {
    return myRunnerParameters;
  }

  @NotNull
  public Module[] getModules() {
    final Module module = ModuleManager.getInstance(getProject()).findModuleByName(myRunnerParameters.getModuleName());
    if (module != null && ModuleType.get(module) instanceof FlexModuleType) {
      return new Module[]{module};
    }
    else {
      return Module.EMPTY_ARRAY;
    }
  }

  private class AirRunState extends CommandLineState {

    public AirRunState(ExecutionEnvironment env) {
      super(env);
    }

    private GeneralCommandLine createCommandLine() throws ExecutionException {
      final Pair<Module, FlexIdeBuildConfiguration> moduleAndConfig = FlexBaseRunner.getModuleAndConfig(getProject(), myRunnerParameters);
      final Module module = moduleAndConfig.first;
      final FlexIdeBuildConfiguration config = moduleAndConfig.second;

      final GeneralCommandLine commandLine = new GeneralCommandLine();

      final SdkEntry sdkEntry = config.DEPENDENCIES.getSdk();
      if (sdkEntry == null) {
        throw new CantRunException(FlexBundle.message("sdk.not.set.for.bc.0.of.module.1", config.NAME, module.getName()));
      }

      commandLine.setExePath(sdkEntry.getHomePath() + FlexSdkUtils.ADL_RELATIVE_PATH);

      final String adlOptions = myRunnerParameters.getAdlOptions();
      if (!StringUtil.isEmptyOrSpaces(adlOptions)) {
        commandLine.addParameters(StringUtil.split(adlOptions, " "));
      }

      final String descriptorName = (config.AIR_DESKTOP_PACKAGING_OPTIONS.USE_GENERATED_DESCRIPTOR
                                     ? BCUtils.getGeneratedAirDescriptorName(config)
                                     : PathUtil.getFileName(config.AIR_DESKTOP_PACKAGING_OPTIONS.CUSTOM_DESCRIPTOR_PATH));
      commandLine.addParameter(config.OUTPUT_FOLDER + "/" + descriptorName);
      commandLine.addParameter(config.OUTPUT_FOLDER);
      final String programParameters = myRunnerParameters.getAirProgramParameters();
      if (!StringUtil.isEmptyOrSpaces(programParameters)) {
        commandLine.addParameter("--");
        commandLine.addParameters(StringUtil.split(programParameters, " "));
      }

      return commandLine;
    }

    @NotNull
    protected OSProcessHandler startProcess() throws ExecutionException {
      return JavaCommandLineStateUtil.startProcess(createCommandLine());
    }
  }
}
