package com.jetbrains.cidr.cpp.embedded.platformio;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.runners.RunConfigurationWithSuppressedDefaultRunAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("WeakerAccess")
public class PlatformioDebugConfiguration extends PlatformioBaseConfiguration
  implements RunConfigurationWithSuppressedDefaultRunAction {

  public PlatformioDebugConfiguration(@NotNull Project project, @NotNull ConfigurationFactory configurationFactory) {
    super(project, configurationFactory, "Debug", () -> ClionEmbeddedPlatformioBundle.message("run.config.debug"), null);
  }

  @NotNull
  @Override
  public String getCmakeBuildTarget() {
    return "Debug";
  }
}
