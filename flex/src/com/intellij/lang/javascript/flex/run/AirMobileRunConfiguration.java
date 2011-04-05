package com.intellij.lang.javascript.flex.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

public class AirMobileRunConfiguration extends AirRunConfiguration {

  public AirMobileRunConfiguration(final Project project, final ConfigurationFactory configurationFactory, final String name) {
    super(project, configurationFactory, name);
  }

  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new AirMobileRunConfigurationForm(getProject());
  }

  public AirMobileRunConfiguration clone() {
    return (AirMobileRunConfiguration)super.clone();
  }

  protected FlexRunnerParameters createRunnerParametersInstance() {
    return new AirMobileRunnerParameters();
  }

  @NotNull
  public AirMobileRunnerParameters getRunnerParameters() {
    return (AirMobileRunnerParameters)super.getRunnerParameters();
  }

  public String suggestedName() {
    return getRunnerParameters().getAirMobileRunMode() == AirMobileRunnerParameters.AirMobileRunMode.MainClass
           ? StringUtil.getShortName(getRunnerParameters().getMainClassName())
           : "unnamed";
  }

  // todo
  public void checkConfiguration() throws RuntimeConfigurationException {
    final AirMobileRunnerParameters params = getRunnerParameters();
    final Module module = getAndValidateModule(getProject(), params.getModuleName());

    switch (params.getAirMobileRunMode()) {
      case AppDescriptor:
        checkAirDescriptorBasedConfiguration(module, params);
        break;
      case MainClass:
        checkMainClassBasedConfiguration(module, params);
        break;
      // todo
      //case AndroidApk:
      //break;
    }

    switch (params.getAirMobileRunTarget()) {
      case Emulator:
        checkAdlAndAirRuntime(module);
        break;
      case AndroidDevice:
        // todo
        break;
    }

    checkDebuggerSdk(params);
  }
}
