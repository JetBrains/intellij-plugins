package com.github.masahirosuzuka.PhoneGapIntelliJPlugin;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by masahiro on 2014/04/04.
 */
public class PhoneGapConfigurationType implements ConfigurationType {
  public PhoneGapConfigurationFactory myConfigurationFactory;

  public PhoneGapConfigurationType() {
    myConfigurationFactory = new PhoneGapConfigurationFactory(this);
  }

  @Override
  public String getDisplayName() {
    return "PhoneGap";
  }

  @Override
  public String getConfigurationTypeDescription() {
    return "PhoneGap Application";
  }

  @Override
  public Icon getIcon() {
    return IconLoader.getIcon("icons/phonegap_13px.png");
  }

  @NotNull
  @Override
  public String getId() {
    return "PhoneGap";
  }

  @Override
  public ConfigurationFactory[] getConfigurationFactories() {
    return new PhoneGapConfigurationFactory[]{ myConfigurationFactory };
  }

  public class PhoneGapConfigurationFactory extends ConfigurationFactory {

    public PhoneGapConfigurationFactory(ConfigurationType type) {
      super(type);
    }

    @Override
    public RunConfiguration createTemplateConfiguration(Project project) {
      return new PhoneGapRunConfiguration(project, myConfigurationFactory, "PhoneGap");
    }
  }
}
