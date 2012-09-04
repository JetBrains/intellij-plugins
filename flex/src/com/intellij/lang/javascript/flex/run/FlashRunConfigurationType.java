package com.intellij.lang.javascript.flex.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import icons.FlexIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class FlashRunConfigurationType implements ConfigurationType {

  public static final String TYPE = "FlashRunConfigurationType";
  public static final String DISPLAY_NAME = "Flash App";

  private final ConfigurationFactory myFactory;

  public FlashRunConfigurationType() {
    myFactory = new ConfigurationFactory(this) {
      public RunConfiguration createTemplateConfiguration(Project project) {
        return new FlashRunConfiguration(project, this, "");
      }
    };
  }

  public String getDisplayName() {
    return DISPLAY_NAME;
  }

  public String getConfigurationTypeDescription() {
    return "Flash run configuration";
  }

  public Icon getIcon() {
    return FlexIcons.Flash_run_config;
  }

  public ConfigurationFactory[] getConfigurationFactories() {
    return new ConfigurationFactory[]{myFactory};
  }

  @NotNull
  public String getId() {
    return TYPE;
  }

  public static FlashRunConfigurationType getInstance() {
    return Extensions.findExtension(CONFIGURATION_TYPE_EP, FlashRunConfigurationType.class);
  }

  public static ConfigurationFactory getFactory() {
    return getInstance().getConfigurationFactories()[0];
  }
}
