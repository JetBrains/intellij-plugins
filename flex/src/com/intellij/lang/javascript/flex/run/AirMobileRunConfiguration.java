package com.intellij.lang.javascript.flex.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import org.jetbrains.annotations.NotNull;

import static com.intellij.lang.javascript.flex.run.AirMobileRunnerParameters.AirMobileRunMode;

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
    final String descriptorPath = getRunnerParameters().getAirDescriptorPath();
    final String packagePath = getRunnerParameters().getExistingPackagePath();
    return getRunnerParameters().getAirMobileRunMode() == AirMobileRunMode.MainClass
           ? StringUtil.getShortName(getRunnerParameters().getMainClassName())
           : getRunnerParameters().getAirMobileRunMode() == AirMobileRunMode.AppDescriptor
             ? descriptorPath.substring(descriptorPath.lastIndexOf('/') + 1)
             : packagePath.substring(packagePath.lastIndexOf('/') + 1);
  }

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
      case ExistingPackage:
        checkExistingPackageBasedConfiguration(params);
        break;
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

  private static void checkExistingPackageBasedConfiguration(final AirMobileRunnerParameters params) throws RuntimeConfigurationError {
    final String path = params.getExistingPackagePath();

    if (StringUtil.isEmptyOrSpaces(path)) {
      throw new RuntimeConfigurationError("Package file path not specified");
    }

    if (!path.toLowerCase().endsWith(".apk")) {
      throw new RuntimeConfigurationError("Package file name must have 'apk' extension");
    }

    if (LocalFileSystem.getInstance().findFileByPath(path) == null) {
      throw new RuntimeConfigurationError(FlexBundle.message("file.not.found", path));
    }

    if (params.getAirMobileRunTarget() != AirMobileRunnerParameters.AirMobileRunTarget.AndroidDevice) {
      throw new RuntimeConfigurationError("Android package can run only on Android device");
    }
  }
}
