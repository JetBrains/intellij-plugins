package com.intellij.coldFusion.mxunit;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.intellij.util.containers.ContainerUtil;
import icons.CFMLIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class CfmlUnitRunConfigurationType implements ConfigurationType {
  private final ConfigurationFactory myFactory;

  public CfmlUnitRunConfigurationType() {
    myFactory = new ConfigurationFactory(this) {
      public RunConfiguration createTemplateConfiguration(Project project) {
        return new CfmlUnitRunConfiguration(project, this, "");
      }
    };
  }

  public String getDisplayName() {
    return "MXUnit";
  }

  public String getConfigurationTypeDescription() {
    return "MXUnit";
  }

  public Icon getIcon() {
    return CFMLIcons.Cfunit;
  }

  @NotNull
  public String getId() {
    return "CfmlUnitRunConfigurationType";
  }

  public ConfigurationFactory[] getConfigurationFactories() {
    return new ConfigurationFactory[]{myFactory};
  }

  @Nullable
  public static CfmlUnitRunConfigurationType getInstance() {
    return ContainerUtil.findInstance(Extensions.getExtensions(CONFIGURATION_TYPE_EP), CfmlUnitRunConfigurationType.class);
  }
}
