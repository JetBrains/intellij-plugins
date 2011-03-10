package com.intellij.lang.javascript.flex.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.lang.javascript.flex.FlexFacetType;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: Maxim.Mossienko
 * Date: Dec 27, 2007
 * Time: 11:21:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class FlexRunConfigurationType implements ConfigurationType {
  private final ConfigurationFactory myFactory;

  public FlexRunConfigurationType() {
    myFactory = new ConfigurationFactory(this) {
      public RunConfiguration createTemplateConfiguration(Project project) {
        return new FlexRunConfiguration(project, this, "");
      }
    };
  }

  public String getDisplayName() {
    return "Flex";
  }

  public String getConfigurationTypeDescription() {
    return "Flex run configuration";
  }

  public Icon getIcon() {
    return FlexFacetType.ourFlexIcon;
  }

  public ConfigurationFactory[] getConfigurationFactories() {
    return new ConfigurationFactory[] {myFactory};
  }

  @NotNull
  public String getId() {
    return "FlexRunConfigurationType";
  }

  public static FlexRunConfigurationType getInstance() {
    return ContainerUtil.findInstance(Extensions.getExtensions(CONFIGURATION_TYPE_EP), FlexRunConfigurationType.class);
  }

  public static ConfigurationFactory getFactory() {
    return getInstance().getConfigurationFactories()[0];
  }
}
