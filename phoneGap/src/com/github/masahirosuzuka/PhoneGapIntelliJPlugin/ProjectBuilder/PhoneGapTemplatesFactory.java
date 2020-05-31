package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ProjectBuilder;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.WebModuleBuilder;
import com.intellij.platform.ProjectTemplate;
import com.intellij.platform.ProjectTemplatesFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class
PhoneGapTemplatesFactory extends ProjectTemplatesFactory {
  @Override
  public String @NotNull [] getGroups() {
    return new String[]{WebModuleBuilder.GROUP_NAME};
  }

  @Override
  public ProjectTemplate @NotNull [] createTemplates(@Nullable String group, WizardContext context) {
    return new ProjectTemplate[]{new CordovaProjectGenerator()};
  }
}
