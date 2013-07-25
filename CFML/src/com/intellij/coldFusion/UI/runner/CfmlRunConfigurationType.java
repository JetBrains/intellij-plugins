package com.intellij.coldFusion.UI.runner;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import icons.CFMLIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by Lera Nikolaenko
 * Date: 07.04.2009
 */
public class CfmlRunConfigurationType implements ConfigurationType {
  private ConfigurationFactory myConfigurationFactory;

  public CfmlRunConfigurationType() {
    myConfigurationFactory = new ConfigurationFactory(this) {
      public RunConfiguration createTemplateConfiguration(Project project) {
        return new CfmlRunConfiguration(project, this, "Cold Fusion");
      }
    };
  }

  public String getDisplayName() {
    return "Cold Fusion";
  }

  public String getConfigurationTypeDescription() {
    return "Cold Fusion runner description";
  }

  public Icon getIcon() {
    return CFMLIcons.Cfml;
  }

  @NotNull
  public String getId() {
    return getConfigurationTypeDescription();
  }

  public ConfigurationFactory[] getConfigurationFactories() {
    return new ConfigurationFactory[]{myConfigurationFactory};
  }
}
