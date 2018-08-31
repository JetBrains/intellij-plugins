// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.SimpleConfigurationType;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.util.LazyUtil;
import icons.FlexIcons;
import org.jetbrains.annotations.NotNull;

public final class FlexUnitRunConfigurationType extends SimpleConfigurationType {
  public FlexUnitRunConfigurationType() {
    super("FlexUnitRunConfigurationType", "FlexUnit", "FlexUnit run configuration", LazyUtil.create(() -> FlexIcons.Flex.Flexunit));
  }

  @Override
  @NotNull
  public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
    return new FlexUnitRunConfiguration(project, this, "");
  }

  @Override
  public boolean isApplicable(@NotNull Project project) {
    return ModuleUtil.hasModulesOfType(project, FlexModuleType.getInstance());
  }
}
