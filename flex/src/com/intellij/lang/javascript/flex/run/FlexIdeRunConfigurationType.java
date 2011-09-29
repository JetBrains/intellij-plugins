package com.intellij.lang.javascript.flex.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class FlexIdeRunConfigurationType implements ConfigurationType {

  private static final Icon ICON = IconLoader.getIcon("flex_ide_run_configuration.png");

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
    return ICON;
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
