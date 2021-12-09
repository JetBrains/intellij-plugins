/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package com.intellij.javascript.karma.execution;

import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationSingletonPolicy;
import com.intellij.execution.configurations.SimpleConfigurationType;
import com.intellij.javascript.karma.KarmaBundle;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NotNullLazyValue;
import icons.KarmaIcons;
import org.jetbrains.annotations.NotNull;

public final class KarmaConfigurationType extends SimpleConfigurationType implements DumbAware {
  public KarmaConfigurationType() {
    super("JavaScriptTestRunnerKarma", KarmaBundle.message("rc.run_configuration_type.name"), null, NotNullLazyValue.createValue(() -> KarmaIcons.Karma2));
  }

  @NotNull
  @Override
  public String getTag() {
    return "karma";
  }

  @Override
  public String getHelpTopic() {
    return "reference.dialogs.rundebug.JavaScriptTestRunnerKarma";
  }

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

  @NotNull
  public static KarmaConfigurationType getInstance() {
    return ConfigurationTypeUtil.findConfigurationType(KarmaConfigurationType.class);
  }

  @Override
  public boolean isEditableInDumbMode() {
    return true;
  }
}
