// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.UI.runner;

import com.intellij.coldFusion.model.files.CfmlFileType;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.SimpleConfigurationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import icons.CFMLIcons;
import org.jetbrains.annotations.NotNull;

public final class CfmlRunConfigurationType extends SimpleConfigurationType {
  public CfmlRunConfigurationType() {
    super("Cold Fusion runner description" /* yes, backward compatibility, so strange id */, "Cold Fusion", "Cold Fusion runner description", //NON-NLS
          NotNullLazyValue.lazy(() -> CFMLIcons.Cfml));
  }

  @Override
  public boolean isApplicable(@NotNull Project project) {
    return FileTypeIndex.containsFileOfType(CfmlFileType.INSTANCE, GlobalSearchScope.projectScope(project));
  }

  @Override
  @NotNull
  public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
    return new CfmlRunConfiguration(project, this, "Cold Fusion");
  }

  @Override
  public String getHelpTopic() {
    return "reference.dialogs.rundebug.Cold Fusion runner description";
  }

  public static CfmlRunConfigurationType getInstance() {
    return ConfigurationTypeUtil.findConfigurationType(CfmlRunConfigurationType.class);
  }
}
