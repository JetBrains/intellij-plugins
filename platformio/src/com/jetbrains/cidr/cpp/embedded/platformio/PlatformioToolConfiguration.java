package com.jetbrains.cidr.cpp.embedded.platformio;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfigurationWithSuppressedDefaultDebugAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsActions;
import com.jetbrains.cidr.cpp.embedded.platformio.ui.PlatformioActionBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@SuppressWarnings("WeakerAccess")
public class PlatformioToolConfiguration extends PlatformioBaseConfiguration
  implements RunConfigurationWithSuppressedDefaultDebugAction {

  public PlatformioToolConfiguration(@NotNull Project project,
                                     @NotNull ConfigurationFactory configurationFactory,
                                     @NotNull Supplier<@NlsActions.ActionText String> name,
                                     PlatformioActionBase.FUS_COMMAND command,
                                     @Nullable String ... cliParameters) {
    super(project, configurationFactory, "Production", name, cliParameters, command);
  }

  @NotNull
  @Override
  public String getCmakeBuildTarget() {
    return "Production";
  }
}
