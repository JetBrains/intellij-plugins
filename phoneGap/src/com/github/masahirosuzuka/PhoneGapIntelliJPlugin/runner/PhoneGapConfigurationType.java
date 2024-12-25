// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.PhoneGapBundle;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import icons.PhoneGapIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class PhoneGapConfigurationType implements ConfigurationType, DumbAware {
  public static final String RUN_CONFIGURATION_ID = "PhoneGap";

  public PhoneGapConfigurationFactory myConfigurationFactory;

  public PhoneGapConfigurationType() {
    myConfigurationFactory = new PhoneGapConfigurationFactory(this);
  }

  @Override
  public @NotNull String getDisplayName() {
    return PhoneGapBundle.message("phonegap.run.configuration.title");
  }

  @Override
  public String getConfigurationTypeDescription() {
    return PhoneGapBundle.message("phonegap.run.configuration.description");
  }

  @Override
  public Icon getIcon() {
    return PhoneGapIcons.PhonegapIntegration;
  }

  @Override
  public @NotNull String getId() {
    return RUN_CONFIGURATION_ID;
  }

  @Override
  public ConfigurationFactory[] getConfigurationFactories() {
    return new PhoneGapConfigurationFactory[]{myConfigurationFactory};
  }

  @Override
  public String getHelpTopic() {
    return "reference.dialogs.rundebug.PhoneGap";
  }

  public class PhoneGapConfigurationFactory extends ConfigurationFactory {
    public PhoneGapConfigurationFactory(ConfigurationType type) {
      super(type);
    }

    @Override
    public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project) {
      return new PhoneGapRunConfiguration(project, myConfigurationFactory, RUN_CONFIGURATION_ID);
    }

    @Override
    public @NotNull String getId() {
      return "PhoneGap/Cordova";
    }
  }
}
