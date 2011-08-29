package com.intellij.lang.javascript.flex.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class FlexIdeRunConfigurationType implements ConfigurationType {

  private final ConfigurationFactory myFactory;

  public FlexIdeRunConfigurationType() {
    myFactory = new ConfigurationFactory(this) {
      public RunConfiguration createTemplateConfiguration(Project project) {
        return new FlexIdeRunConfiguration(project, this, "");
      }
    };
  }

  public String getDisplayName() {
    return "Flash App";
  }

  public String getConfigurationTypeDescription() {
    return "Flash run configuration";
  }

  public Icon getIcon() {
    //todo how to show different icons for different run configurations? (based of target platform of BC)
    return PlatformIcons.CUSTOM_FILE_ICON;
  }

  public ConfigurationFactory[] getConfigurationFactories() {
    return new ConfigurationFactory[]{myFactory};
  }

  @NotNull
  public String getId() {
    return "FlexIdeRunConfigurationType";
  }

  public static FlexIdeRunConfigurationType getInstance() {
    return Extensions.findExtension(CONFIGURATION_TYPE_EP, FlexIdeRunConfigurationType.class);
  }

  public static ConfigurationFactory getFactory() {
    return getInstance().getConfigurationFactories()[0];
  }
}
