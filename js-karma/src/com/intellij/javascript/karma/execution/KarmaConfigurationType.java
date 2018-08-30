// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.karma.execution;

import com.intellij.execution.configurations.*;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import icons.JSKarmaIcons;
import org.jetbrains.annotations.NotNull;

public final class KarmaConfigurationType extends ConfigurationTypeBase implements DumbAware {
  public KarmaConfigurationType() {
    super("JavaScriptTestRunnerKarma", "Karma", "Karma", JSKarmaIcons.Icons.Karma2);
    addFactory(new ConfigurationFactory(this) {
      @NotNull
      @Override
      public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new KarmaRunConfiguration(project, this, "Karma");
      }

      @NotNull
      @Override
      public RunConfigurationSingletonPolicy getSingletonPolicy() {
        return RunConfigurationSingletonPolicy.SINGLE_INSTANCE_ONLY;
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
