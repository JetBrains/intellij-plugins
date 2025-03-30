// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.components.BaseState;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public final class JdlRunConfigurationTypeFactory extends ConfigurationFactory {
  public JdlRunConfigurationTypeFactory(JdlRunConfigurationType type) {
    super(type);
  }

  @Override
  public @NotNull @NonNls String getId() {
    return JdlRunConfigurationType.ID;
  }

  @Override
  public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project) {
    return new JdlRunConfiguration(project, this, "JHipster Generate");
  }

  @Override
  public @NotNull Class<? extends BaseState> getOptionsClass() {
    return JdlRunConfigurationOptions.class;
  }

  @Override
  public boolean isEditableInDumbMode() {
    return true;
  }
}