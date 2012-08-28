package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class FlexUnitRunConfigurationType implements ConfigurationType {

  private static final Icon ourIcon = IconLoader.getIcon("/images/flex/flexunit.png", FlexUnitRunConfigurationType.class);

  private final ConfigurationFactory myFactory;

  public FlexUnitRunConfigurationType() {
    myFactory = new ConfigurationFactory(this) {
      public RunConfiguration createTemplateConfiguration(Project project) {
        return new FlexUnitRunConfiguration(project, this, "");
      }
    };
  }

  public String getDisplayName() {
    return "FlexUnit";
  }

  public String getConfigurationTypeDescription() {
    return "FlexUnit run configuration";
  }

  public Icon getIcon() {
    return ourIcon;
  }

  public ConfigurationFactory[] getConfigurationFactories() {
    return new ConfigurationFactory[]{myFactory};
  }

  @NotNull
  public String getId() {
    return "FlexUnitRunConfigurationType";
  }

  public static FlexUnitRunConfigurationType getInstance() {
    return Extensions.findExtension(CONFIGURATION_TYPE_EP, FlexUnitRunConfigurationType.class);
  }

  public static ConfigurationFactory getFactory() {
    return getInstance().getConfigurationFactories()[0];
  }
}
