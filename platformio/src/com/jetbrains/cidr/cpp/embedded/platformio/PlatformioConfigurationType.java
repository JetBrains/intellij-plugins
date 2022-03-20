package com.jetbrains.cidr.cpp.embedded.platformio;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationSingletonPolicy;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsActions;
import com.intellij.openapi.util.NotNullLazyValue;
import com.jetbrains.cidr.cpp.embedded.platformio.ui.EmptyEditor;
import com.jetbrains.cidr.cpp.embedded.platformio.ui.PlatformioActionBase;
import com.jetbrains.cidr.cpp.embedded.platformio.ui.PlatformioActionBase.FUS_COMMAND;
import com.jetbrains.cidr.cpp.execution.CMakeAppRunConfiguration;
import com.jetbrains.cidr.cpp.execution.CMakeRunConfigurationType;
import icons.ClionEmbeddedPlatformioIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

import static com.intellij.execution.configurations.RunConfigurationSingletonPolicy.SINGLE_INSTANCE_ONLY;
import static com.jetbrains.cidr.cpp.embedded.platformio.ui.PlatformioActionBase.FUS_COMMAND.*;
import static icons.ClionEmbeddedPlatformioIcons.Platformio;

public class PlatformioConfigurationType extends CMakeRunConfigurationType {

  public static final String PLATFORM_IO_DEBUG_ID = "PlatformIO Debug";
  public static final String TYPE_ID = "platformio";
  private final ConfigurationFactory[] myNewProjectFactories;

  public PlatformioConfigurationType() {
    super(TYPE_ID, PLATFORM_IO_DEBUG_ID, ClionEmbeddedPlatformioBundle.message("platformio.name"),
          ClionEmbeddedPlatformioBundle.message("platformio.description"),
          NotNullLazyValue.createValue(() -> Platformio));

    final var uploadFactory = getConfigurationFactories()[0];
    final var uploadConfigurationFactory =
      new ToolConfigurationFactory("PlatformIO Upload", () -> ClionEmbeddedPlatformioBundle.message("run.config.upload"), UPLOAD, "-c",
                                   "clion",
                                   "run", "--target", "upload");

    addFactory(
      new ToolConfigurationFactory("PlatformIO Test", () -> ClionEmbeddedPlatformioBundle.message("run.config.test"), TEST, "-c", "clion",
                                   "test"));
    addFactory(uploadConfigurationFactory);
    addFactory(
      new ToolConfigurationFactory("PlatformIO Program", () -> ClionEmbeddedPlatformioBundle.message("run.config.program"), PROGRAM, "-c",
                                   "clion",
                                   "run", "--target", "program"));
    addFactory(
      new ToolConfigurationFactory("PlatformIO Uploadfs", () -> ClionEmbeddedPlatformioBundle.message("run.config.uploadfs"), UPLOADFS,
                                   "-c", "clion",
                                   "run", "--target", "uploadfs"));
    myNewProjectFactories = new ConfigurationFactory[]{uploadFactory, uploadConfigurationFactory};
  }

  @NotNull
  public ConfigurationFactory[] getNewProjectFactories() {
    return myNewProjectFactories;
  }

  @NotNull
  @Override
  public ConfigurationFactory getFactory() {
    return new ConfigurationFactory(this) {
      @NotNull
      @Override
      public RunConfiguration createTemplateConfiguration(final @NotNull Project project) {
        return new PlatformioDebugConfiguration(project, this);
      }

      @NotNull
      @Override
      public RunConfigurationSingletonPolicy getSingletonPolicy() {
        return SINGLE_INSTANCE_ONLY;
      }

      @NotNull
      @Override
      public String getName() {
        return ClionEmbeddedPlatformioBundle.message("run.config.debug");
      }

      @Override
      public @NotNull
      String getId() {
        return PLATFORM_IO_DEBUG_ID;
      }

      @Override
      public boolean isEditableInDumbMode() {
        return true;
      }
    };
  }

  @Override
  public SettingsEditor<? extends CMakeAppRunConfiguration> createEditor(final @NotNull Project project) {
    return new EmptyEditor<>();
  }

  @Override
  @NotNull
  protected PlatformioDebugConfiguration createRunConfiguration(final @NotNull Project project,
                                                                final @NotNull ConfigurationFactory configurationFactory) {
    return new PlatformioDebugConfiguration(project, configurationFactory);
  }

  @Override
  public @Nullable String getHelpTopic() {
    return "rundebugconfigs.platformio";
  }

  public class ToolConfigurationFactory extends ConfigurationFactory {
    private final String[] cliParameters;
    private final Supplier<@NlsActions.ActionText String> name;
    private final String myId;
    private final FUS_COMMAND command;

    ToolConfigurationFactory(final @NonNls @NotNull String id,
                             final @NotNull Supplier<@NlsActions.ActionText String> name,
                             final FUS_COMMAND command,
                             final String... cliParameters) {
      super(PlatformioConfigurationType.this);
      this.myId = id;
      this.name = name;
      this.command = command;
      this.cliParameters = cliParameters;
    }

    @Override
    public @NotNull
    RunConfiguration createTemplateConfiguration(final @NotNull Project project) {
      return new PlatformioToolConfiguration(project, this, name, command, cliParameters);
    }

    @Override
    public @NotNull String getId() {
      return myId;
    }

    @Override
    public @NotNull
    String getName() {
      return name.get();
    }

    @Override
    public boolean isEditableInDumbMode() {
      return true;
    }
  }
}
