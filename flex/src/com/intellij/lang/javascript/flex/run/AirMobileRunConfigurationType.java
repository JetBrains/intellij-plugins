package com.intellij.lang.javascript.flex.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.lang.javascript.flex.sdk.AirMobileSdkType;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class AirMobileRunConfigurationType implements ConfigurationType {
  private final ConfigurationFactory myFactory;

  public AirMobileRunConfigurationType() {
    myFactory = new ConfigurationFactory(this) {
      public RunConfiguration createTemplateConfiguration(Project project) {
        return new AirMobileRunConfiguration(project, this, "");
      }
    };
  }

  public String getDisplayName() {
    return "AIR Mobile";
  }

  public String getConfigurationTypeDescription() {
    return "AIR Mobile run configuration";
  }

  public Icon getIcon() {
    return AirMobileSdkType.airMobileIcon;
  }

  public ConfigurationFactory[] getConfigurationFactories() {
    return new ConfigurationFactory[]{myFactory};
  }

  @NotNull
  public String getId() {
    return "AirMobileRunConfigurationType";
  }

  public static AirMobileRunConfigurationType getInstance() {
    return ContainerUtil.findInstance(Extensions.getExtensions(CONFIGURATION_TYPE_EP), AirMobileRunConfigurationType.class);
  }
}
