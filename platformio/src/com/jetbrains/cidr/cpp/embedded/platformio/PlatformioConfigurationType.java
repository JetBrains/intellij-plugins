package com.jetbrains.cidr.cpp.embedded.platformio;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationSingletonPolicy;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NotNullLazyValue;
import com.jetbrains.cidr.cpp.embedded.platformio.ui.EmptyEditor;
import com.jetbrains.cidr.cpp.execution.CMakeAppRunConfiguration;
import com.jetbrains.cidr.cpp.execution.CMakeRunConfigurationType;
import icons.ClionEmbeddedPlatformioIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class PlatformioConfigurationType extends CMakeRunConfigurationType {

  public static final String PLATFORM_IO_DEBUG_ID = "PlatformIO Debug";
  private static final String TYPE_ID = "platformio";

  public PlatformioConfigurationType() {
    super(TYPE_ID, PLATFORM_IO_DEBUG_ID, "PlatformIO", "PlatformIO",
          NotNullLazyValue.createValue(() -> ClionEmbeddedPlatformioIcons.Platformio));
    addFactory(new ToolConfigurationFactory("PlatformIO Test", "PlatformIO Test", "-c", "clion", "test"));
    addFactory(new ToolConfigurationFactory("PlatformIO Upload", "PlatformIO Upload", "-c", "clion", "run", "--target", "upload"));
    addFactory(new ToolConfigurationFactory("PlatformIO Program", "PlatformIO Program", "-c", "clion", "run", "--target", "program"));
    addFactory(new ToolConfigurationFactory("PlatformIO Uploadfs", "PlatformIO Uploadfs", "-c", "clion", "run", "--target", "uploadfs"));
  }

  @NotNull
  @Override
  public ConfigurationFactory getFactory() {
    return new ConfigurationFactory(this) {
      @NotNull
      @Override
      public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new PlatformioDebugConfiguration(project, this);
      }

      @NotNull
      @Override
      public RunConfigurationSingletonPolicy getSingletonPolicy() {
        return RunConfigurationSingletonPolicy.SINGLE_INSTANCE_ONLY;
      }

      @NotNull
      @Override
      public String getName() {
        return PLATFORM_IO_DEBUG_ID;
      }

      @Override
      public @NotNull
      String getId() {
        return PLATFORM_IO_DEBUG_ID;
      }
    };
  }

  @Override
  public SettingsEditor<? extends CMakeAppRunConfiguration> createEditor(@NotNull Project project) {
    return new EmptyEditor<>();
  }

  @Override
  @NotNull
  protected PlatformioDebugConfiguration createRunConfiguration(@NotNull Project project,
                                                                @NotNull ConfigurationFactory configurationFactory) {
    return new PlatformioDebugConfiguration(project, configurationFactory);
  }

  private class ToolConfigurationFactory extends ConfigurationFactory {
    private final String[] cliParameters;
    private final String name;
    private final String myId;

    ToolConfigurationFactory(@NotNull String name, @NonNls @NotNull String id, String... cliParameters) {
      super(PlatformioConfigurationType.this);
      this.name = name;
      this.cliParameters = cliParameters;
      myId = id;
    }

    @Override
    public @NotNull
    RunConfiguration createTemplateConfiguration(@NotNull Project project) {
      return new PlatformioToolConfiguration(project, this, name, cliParameters);
    }

    @Override
    public @NotNull
    String getName() {
      return name;
    }

    @Override
    public @NotNull String getId() {
      return myId;
    }
  }
}
