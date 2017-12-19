package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import icons.FlexIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class FlexUnitRunConfigurationType implements ConfigurationType {

  private final ConfigurationFactory myFactory;

  public FlexUnitRunConfigurationType() {
    myFactory = new ConfigurationFactory(this) {
      @NotNull
      public RunConfiguration createTemplateConfiguration(Project project) {
        return new FlexUnitRunConfiguration(project, this, "");
      }

      @Override
      public boolean isApplicable(@NotNull Project project) {
        return ModuleUtil.hasModulesOfType(project, FlexModuleType.getInstance());
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
    return FlexIcons.Flex.Flexunit;
  }

  public ConfigurationFactory[] getConfigurationFactories() {
    return new ConfigurationFactory[]{myFactory};
  }

  @NotNull
  public String getId() {
    return "FlexUnitRunConfigurationType";
  }

  public static FlexUnitRunConfigurationType getInstance() {
    return ConfigurationTypeUtil.findConfigurationType(FlexUnitRunConfigurationType.class);
  }

  public static ConfigurationFactory getFactory() {
    return getInstance().getConfigurationFactories()[0];
  }
}
