package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ProjectBuilder;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.WebModuleBuilder;
import com.intellij.platform.ProjectTemplate;
import com.intellij.platform.ProjectTemplatesFactory;
import org.jetbrains.annotations.NotNull;

public class PhoneGapTemplatesFactory extends ProjectTemplatesFactory {
  @NotNull
  @Override
  public String[] getGroups() {
    return new String[]{WebModuleBuilder.GROUP_NAME};
  }

  @NotNull
  @Override
  public ProjectTemplate[] createTemplates(String group, WizardContext context) {
    return new ProjectTemplate[]{new PhoneGapProjectTemplateGenerator()};
  }
}
