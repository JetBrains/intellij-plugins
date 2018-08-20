// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
      @Override
      @NotNull
      public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new FlexUnitRunConfiguration(project, this, "");
      }

      @Override
      public boolean isApplicable(@NotNull Project project) {
        return ModuleUtil.hasModulesOfType(project, FlexModuleType.getInstance());
      }
    };
  }

  @Override
  public String getDisplayName() {
    return "FlexUnit";
  }

  @Override
  public String getConfigurationTypeDescription() {
    return "FlexUnit run configuration";
  }

  @Override
  public Icon getIcon() {
    return FlexIcons.Flex.Flexunit;
  }

  @Override
  public ConfigurationFactory[] getConfigurationFactories() {
    return new ConfigurationFactory[]{myFactory};
  }

  @Override
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
