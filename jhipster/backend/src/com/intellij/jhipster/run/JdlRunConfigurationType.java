// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.jhipster.JdlBundle;
import com.intellij.jhipster.JdlIconsMapping;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public final class JdlRunConfigurationType implements ConfigurationType, DumbAware {
  public static final String ID = "JHipsterJDL";

  @Override
  public @NotNull @NonNls String getId() {
    return ID;
  }

  @Override
  public @NotNull @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
    return JdlBundle.message("jhipster");
  }

  @Override
  public String getConfigurationTypeDescription() {
    return JdlBundle.message("jhipster.jdl.generator");
  }

  @Override
  public Icon getIcon() {
    return JdlIconsMapping.FILE_ICON;
  }

  @Override
  public ConfigurationFactory[] getConfigurationFactories() {
    return new ConfigurationFactory[]{new JdlRunConfigurationTypeFactory(this)};
  }

  public static JdlRunConfigurationType getInstance() {
    return ConfigurationTypeUtil.findConfigurationType(JdlRunConfigurationType.class);
  }
}