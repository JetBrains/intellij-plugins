// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.components.BaseState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NotNullLazyValue;
import icons.CucumberJavaIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.java.CucumberJavaBundle;

import javax.swing.*;

public final class CucumberJavaRunConfigurationType extends ConfigurationTypeBase {
  public CucumberJavaRunConfigurationType() {
    super("CucumberJavaRunConfigurationType", CucumberJavaBundle.message("cucumber.java.run.configuration.type.name"), null,
          NotNullLazyValue.createValue(() -> CucumberJavaIcons.CucumberJavaRunConfiguration));
    addFactory(new ConfigurationFactory(this) {
      @Override
      public Icon getIcon() {
        return CucumberJavaRunConfigurationType.this.getIcon();
      }

      @Override
      public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new CucumberJavaRunConfiguration(getDisplayName(), project, this);
      }

      @Override
      public Class<? extends BaseState> getOptionsClass() {
        return CucumberJavaConfigurationOptions.class;
      }

      @Override
      public @NotNull String getId() {
        return "Cucumber java";
      }
    });
  }

  @Override
  public String getHelpTopic() {
    return "reference.dialogs.rundebug.CucumberJavaRunConfigurationType";
  }

  public static @NotNull CucumberJavaRunConfigurationType getInstance() {
    return ConfigurationTypeUtil.findConfigurationType(CucumberJavaRunConfigurationType.class);
  }
}
