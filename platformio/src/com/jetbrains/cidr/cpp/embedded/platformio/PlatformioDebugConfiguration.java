package com.jetbrains.cidr.cpp.embedded.platformio;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.runners.RunConfigurationWithSuppressedDefaultRunAction;
import com.intellij.openapi.project.Project;
import com.jetbrains.cidr.cpp.embedded.platformio.ui.PlatformioActionBase;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.cidr.cpp.embedded.platformio.ui.PlatformioActionBase.FUS_COMMAND.DEBUG;

@SuppressWarnings("WeakerAccess")
public class PlatformioDebugConfiguration extends PlatformioBaseConfiguration
  implements RunConfigurationWithSuppressedDefaultRunAction {

  public PlatformioDebugConfiguration(
          final @NotNull Project project,
          final @NotNull ConfigurationFactory configurationFactory) {
    super(project, configurationFactory, "Debug", () -> ClionEmbeddedPlatformioBundle.message("run.config.debug"),null, DEBUG);
  }

  @NotNull
  @Override
  public String getCmakeBuildTarget() {
    return "Debug";
  }
}
