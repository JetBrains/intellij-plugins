package org.jetbrains.plugins.cucumber.groovy.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import icons.CucumberGroovyIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.runner.GroovyScriptRunConfigurationType;

import javax.swing.*;

public class GrCucumberRunConfigurationType implements ConfigurationType {
  public static GroovyScriptRunConfigurationType getInstance() {
    return ConfigurationTypeUtil.findConfigurationType(GroovyScriptRunConfigurationType.class);
  }

  @Override
  public String getDisplayName() {
    return "Groovy cucumber";
  }

  @Override
  public String getConfigurationTypeDescription() {
    return "Cucumber feature for Groovy";
  }

  @Override
  public Icon getIcon() {
    return CucumberGroovyIcons.CucumberGroovyRunConfiguration;
  }

  @NotNull
  @Override
  public String getId() {
    return "GrCucumberRunConfiguration";
  }

  @Override
  public ConfigurationFactory[] getConfigurationFactories() {
    return new ConfigurationFactory[]{myFactory};
  }

  private final ConfigurationFactory myFactory = new MyFactory(this);

  private static class MyFactory extends ConfigurationFactory {
    public MyFactory(ConfigurationType type) {
      super(type);
    }

    public RunConfiguration createTemplateConfiguration(Project project) {
      return new GrCucumberRunConfiguration("Groovy cucumber", project, this);
    }
  }
}
