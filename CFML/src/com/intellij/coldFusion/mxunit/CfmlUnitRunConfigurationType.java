// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.mxunit;

import com.intellij.coldFusion.model.files.CfmlFileType;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import icons.CFMLIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class CfmlUnitRunConfigurationType implements ConfigurationType {
  private final ConfigurationFactory myFactory;

  public CfmlUnitRunConfigurationType() {
    myFactory = new ConfigurationFactory(this) {
      @Override
      public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new CfmlUnitRunConfiguration(project, this, "");
      }

      @Override
      public boolean isApplicable(@NotNull Project project) {
        return FileTypeIndex.containsFileOfType(CfmlFileType.INSTANCE, GlobalSearchScope.projectScope(project));
      }

      @Override
      public @NotNull String getId() {
        return "MXUnit";
      }
    };
  }

  @Override
  public @NotNull String getDisplayName() {
    return "MXUnit"; //NON-NLS
  }

  @Override
  public String getConfigurationTypeDescription() {
    return "MXUnit"; //NON-NLS
  }

  @Override
  public Icon getIcon() {
    return CFMLIcons.Cfunit;
  }

  @Override
  public @NotNull String getId() {
    return "CfmlUnitRunConfigurationType";
  }

  @Override
  public ConfigurationFactory[] getConfigurationFactories() {
    return new ConfigurationFactory[]{myFactory};
  }

  @Override
  public String getHelpTopic() {
    return "reference.dialogs.rundebug.CfmlUnitRunConfigurationType";
  }

  public static @NotNull CfmlUnitRunConfigurationType getInstance() {
    return ConfigurationTypeUtil.findConfigurationType(CfmlUnitRunConfigurationType.class);
  }
}
