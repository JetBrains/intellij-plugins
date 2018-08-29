// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.UI.runner;

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

/**
 * Created by Lera Nikolaenko
 */
public class CfmlRunConfigurationType implements ConfigurationType {
  private final ConfigurationFactory myConfigurationFactory;

  public CfmlRunConfigurationType() {
    myConfigurationFactory = new ConfigurationFactory(this) {
      @Override
      public boolean isApplicable(@NotNull Project project) {
        return FileTypeIndex.containsFileOfType(CfmlFileType.INSTANCE, GlobalSearchScope.projectScope(project));
      }

      @Override
      @NotNull
      public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new CfmlRunConfiguration(project, this, "Cold Fusion");
      }
    };
  }


  @NotNull
  @Override
  public String getDisplayName() {
    return "Cold Fusion";
  }

  @Override
  public String getConfigurationTypeDescription() {
    return "Cold Fusion runner description";
  }

  @Override
  public Icon getIcon() {
    return CFMLIcons.Cfml;
  }

  public static CfmlRunConfigurationType getInstance() {
    return ConfigurationTypeUtil.findConfigurationType(CfmlRunConfigurationType.class);
  }

  @Override
  @NotNull
  public String getId() {
    return getConfigurationTypeDescription();
  }

  @Override
  public ConfigurationFactory[] getConfigurationFactories() {
    return new ConfigurationFactory[]{myConfigurationFactory};
  }

}
