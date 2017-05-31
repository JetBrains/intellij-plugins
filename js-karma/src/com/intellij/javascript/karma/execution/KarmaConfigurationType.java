package com.intellij.javascript.karma.execution;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import icons.JSKarmaIcons;
import org.jetbrains.annotations.NotNull;

public class KarmaConfigurationType extends ConfigurationTypeBase {

  public KarmaConfigurationType() {
    super("JavaScriptTestRunnerKarma", "Karma", "Karma", JSKarmaIcons.Karma2);
    addFactory(new ConfigurationFactory(this) {
      @NotNull
      @Override
      public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new KarmaRunConfiguration(project, this, "Karma");
      }

      @Override
      public boolean isConfigurationSingletonByDefault() {
        return true;
      }

      @Override
      public boolean canConfigurationBeSingleton() {
        return false;
      }
    });
  }

  @NotNull
  public static KarmaConfigurationType getInstance() {
    return Holder.INSTANCE;
  }

  @NotNull
  public static ConfigurationFactory getFactory() {
    KarmaConfigurationType type = getInstance();
    return type.getConfigurationFactories()[0];
  }

  private static class Holder {
    private static final KarmaConfigurationType INSTANCE = ConfigurationTypeUtil.findConfigurationType(KarmaConfigurationType.class);
  }
}
