package com.jetbrains.cidr.cpp.embedded.platformio;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfigurationWithSuppressedDefaultDebugAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@SuppressWarnings("WeakerAccess")
public class PlatformioToolConfiguration extends PlatformioBaseConfiguration
  implements RunConfigurationWithSuppressedDefaultDebugAction {

  public PlatformioToolConfiguration(@NotNull Project project,
                                     @NotNull ConfigurationFactory configurationFactory,
                                     @NotNull Supplier<String> name,
                                     String @Nullable ... cliParameters) {
    super(project, configurationFactory, "Production", name, cliParameters);
  }

  @NotNull
  @Override
  public String getCmakeBuildTarget() {
    return "Production";
  }
}
