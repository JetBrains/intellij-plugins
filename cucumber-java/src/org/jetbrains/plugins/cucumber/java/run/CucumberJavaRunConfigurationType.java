// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.execution.configuration.ConfigurationFactoryEx;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.components.BaseState;
import com.intellij.openapi.project.Project;
import com.intellij.util.LazyUtil;
import icons.CucumberJavaIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class CucumberJavaRunConfigurationType extends ConfigurationTypeBase {
  public CucumberJavaRunConfigurationType() {
    super("CucumberJavaRunConfigurationType", "Cucumber java", null, LazyUtil.create(() -> CucumberJavaIcons.CucumberJavaRunConfiguration));
    addFactory(new ConfigurationFactoryEx(this) {
      @Override
      public Icon getIcon() {
        return CucumberJavaRunConfigurationType.this.getIcon();
      }

      @Override
      @NotNull
      public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new CucumberJavaRunConfiguration(getDisplayName(), project, this);
      }

      @Override
      public void onNewConfigurationCreated(@NotNull RunConfiguration configuration) {
        ((ModuleBasedConfiguration)configuration).onNewConfigurationCreated();
      }

      @Override
      public Class<? extends BaseState> getOptionsClass() {
        return CucumberJavaConfigurationOptions.class;
      }
    });
  }

  @NotNull
  public static CucumberJavaRunConfigurationType getInstance() {
    return ConfigurationTypeUtil.findConfigurationType(CucumberJavaRunConfigurationType.class);
  }
}
