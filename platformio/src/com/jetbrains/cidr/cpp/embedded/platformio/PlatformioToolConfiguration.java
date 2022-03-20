package com.jetbrains.cidr.cpp.embedded.platformio;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfigurationWithSuppressedDefaultDebugAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsActions;
import com.jetbrains.cidr.cpp.embedded.platformio.ui.PlatformioActionBase;
import com.jetbrains.cidr.cpp.embedded.platformio.ui.PlatformioActionBase.FUS_COMMAND;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@SuppressWarnings("WeakerAccess")
public class PlatformioToolConfiguration extends PlatformioBaseConfiguration
  implements RunConfigurationWithSuppressedDefaultDebugAction {

  public PlatformioToolConfiguration(final @NotNull Project project,
                                     final @NotNull ConfigurationFactory configurationFactory,
                                     final @NotNull Supplier<@NlsActions.ActionText String> name,
                                     final FUS_COMMAND command,
                                     final @Nullable String ... cliParameters) {
    super(project, configurationFactory, "Production", name, cliParameters, command);
  }

  @NotNull
  @Override
  public String getCmakeBuildTarget() {
    return "Production";
  }
}
