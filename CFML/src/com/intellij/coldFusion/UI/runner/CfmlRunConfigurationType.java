// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.UI.runner;

import com.intellij.coldFusion.model.files.CfmlFileType;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.LazyUtil;
import icons.CFMLIcons;
import org.jetbrains.annotations.NotNull;

public final class CfmlRunConfigurationType extends ConfigurationTypeBase {
  public CfmlRunConfigurationType() {
    super("Cold Fusion runner description" /* yes, backward compatibility, so strange id */, "Cold Fusion", "Cold Fusion runner description", LazyUtil.create(() -> CFMLIcons.Cfml));
    addFactory(new ConfigurationFactory(this) {
      @Override
      public boolean isApplicable(@NotNull Project project) {
        return FileTypeIndex.containsFileOfType(CfmlFileType.INSTANCE, GlobalSearchScope.projectScope(project));
      }

      @Override
      @NotNull
      public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new CfmlRunConfiguration(project, this, "Cold Fusion");
      }
    });
  }

  public static CfmlRunConfigurationType getInstance() {
    return ConfigurationTypeUtil.findConfigurationType(CfmlRunConfigurationType.class);
  }
}
