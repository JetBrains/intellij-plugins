// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.run;

import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.SimpleConfigurationType;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NotNullLazyValue;
import icons.FlexIcons;
import org.jetbrains.annotations.NotNull;

public final class FlashRunConfigurationType extends SimpleConfigurationType {
  public FlashRunConfigurationType() {
    super("FlashRunConfigurationType", "Flash App", "Flash run configuration",
          NotNullLazyValue.createValue(() -> FlexIcons.Flash_run_config));
  }

  @Override
  @NotNull
  public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
    return new FlashRunConfiguration(project, this, "");
  }

  @Override
  public boolean isApplicable(@NotNull Project project) {
    return ModuleUtil.hasModulesOfType(project, FlexModuleType.getInstance());
  }

  @Override
  public String getHelpTopic() {
    return "reference.dialogs.rundebug.FlashRunConfigurationType";
  }

  public static FlashRunConfigurationType getInstance() {
    return ConfigurationTypeUtil.findConfigurationType(FlashRunConfigurationType.class);
  }
}
