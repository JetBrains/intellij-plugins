// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.run;

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

public final class FlashRunConfigurationType implements ConfigurationType {
  public static final String TYPE = "FlashRunConfigurationType";
  public static final String DISPLAY_NAME = "Flash App";

  private final ConfigurationFactory myFactory;

  public FlashRunConfigurationType() {
    myFactory = new ConfigurationFactory(this) {
      @Override
      @NotNull
      public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new FlashRunConfiguration(project, this, "");
      }

      @Override
      public boolean isApplicable(@NotNull Project project) {
        return ModuleUtil.hasModulesOfType(project, FlexModuleType.getInstance());
      }
    };
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return DISPLAY_NAME;
  }

  @Override
  public String getConfigurationTypeDescription() {
    return "Flash run configuration";
  }

  @Override
  public Icon getIcon() {
    return FlexIcons.Flash_run_config;
  }

  @Override
  public ConfigurationFactory[] getConfigurationFactories() {
    return new ConfigurationFactory[]{myFactory};
  }

  @Override
  @NotNull
  public String getId() {
    return TYPE;
  }

  public static FlashRunConfigurationType getInstance() {
    return ConfigurationTypeUtil.findConfigurationType(FlashRunConfigurationType.class);
  }

  public static ConfigurationFactory getFactory() {
    return getInstance().getConfigurationFactories()[0];
  }
}
