package com.intellij.lang.javascript.flex.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.lang.javascript.flex.sdk.AirSdkType;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class AirRunConfigurationType implements ConfigurationType {
  private final ConfigurationFactory myFactory;

  public AirRunConfigurationType() {
    myFactory = new ConfigurationFactory(this) {
      public RunConfiguration createTemplateConfiguration(Project project) {
        return new AirRunConfiguration(project, this, "");
      }
    };
  }

  public String getDisplayName() {
    return "AIR";
  }

  public String getConfigurationTypeDescription() {
    return "AIR run configuration";
  }

  public Icon getIcon() {
    return AirSdkType.airIcon;
  }

  public ConfigurationFactory[] getConfigurationFactories() {
    return new ConfigurationFactory[]{myFactory};
  }

  @NotNull
  public String getId() {
    return "AirRunConfigurationType";
  }

  public static AirRunConfigurationType getInstance() {
    return Extensions.findExtension(CONFIGURATION_TYPE_EP, AirRunConfigurationType.class);
  }

  public static ConfigurationFactory getFactory() {
    return getInstance().getConfigurationFactories()[0];
  }
}
