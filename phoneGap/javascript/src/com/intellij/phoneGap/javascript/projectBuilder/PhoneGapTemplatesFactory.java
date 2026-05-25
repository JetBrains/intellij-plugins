// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.phoneGap.javascript.projectBuilder;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.WebModuleBuilder;
import com.intellij.platform.ProjectTemplate;
import com.intellij.platform.ProjectTemplatesFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class
PhoneGapTemplatesFactory extends ProjectTemplatesFactory {
  @Override
  public String @NotNull [] getGroups() {
    return new String[]{WebModuleBuilder.GROUP_NAME};
  }

  @Override
  public ProjectTemplate @NotNull [] createTemplates(@Nullable String group, @NotNull WizardContext context) {
    return new ProjectTemplate[]{new CordovaProjectGenerator()};
  }
}
