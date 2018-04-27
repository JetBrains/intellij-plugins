package com.intellij.javascript.karma.execution;

import com.intellij.execution.configuration.ConfigurationFactoryEx;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import icons.JSKarmaIcons;
import org.jetbrains.annotations.NotNull;

public class KarmaConfigurationType extends ConfigurationTypeBase implements DumbAware {

  public KarmaConfigurationType() {
    super("JavaScriptTestRunnerKarma", "Karma", "Karma", JSKarmaIcons.Karma2);
    addFactory(new ConfigurationFactoryEx<KarmaRunConfiguration>(this) {
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

      @Override
      public void onNewConfigurationCreated(@NotNull KarmaRunConfiguration configuration) {
        configuration.onNewConfigurationCreated();
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
