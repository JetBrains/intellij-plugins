package com.intellij.lang.javascript.flex.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import icons.FlexIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class RemoteFlashRunConfigurationType implements ConfigurationType {

  private static final Icon ICON = FlexIcons.Flash_remote_debug;
  public static final String TYPE = "RemoteFlashRunConfigurationType";
  public static final String DISPLAY_NAME = "Flash Remote Debug";

  private final ConfigurationFactory myFactory;

  public RemoteFlashRunConfigurationType() {
    myFactory = new ConfigurationFactory(this) {
      public RunConfiguration createTemplateConfiguration(Project project) {
        return new RemoteFlashRunConfiguration(project, this, "");
      }
    };
  }

  public String getDisplayName() {
    return DISPLAY_NAME;
  }

  public String getConfigurationTypeDescription() {
    return "Remote Flash debug configuration";
  }

  public Icon getIcon() {
    return ICON;
  }

  public ConfigurationFactory[] getConfigurationFactories() {
    return new ConfigurationFactory[]{myFactory};
  }

  @NotNull
  public String getId() {
    return TYPE;
  }

  public static RemoteFlashRunConfigurationType getInstance() {
    return Extensions.findExtension(CONFIGURATION_TYPE_EP, RemoteFlashRunConfigurationType.class);
  }

  public static ConfigurationFactory getFactory() {
    return getInstance().getConfigurationFactories()[0];
  }
}
