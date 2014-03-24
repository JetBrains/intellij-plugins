package com.jetbrains.lang.dart.ide.template;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.WebModuleBuilder;
import com.intellij.platform.ProjectTemplate;
import com.intellij.platform.ProjectTemplatesFactory;
import org.jetbrains.annotations.NotNull;

public class DartTemplatesFactory extends ProjectTemplatesFactory {

  @NotNull
  @Override
  public String[] getGroups() {
    return new String[]{WebModuleBuilder.GROUP_NAME};
  }

  @NotNull
  @Override
  public ProjectTemplate[] createTemplates(String group, WizardContext context) {
    return new ProjectTemplate[]{
      new DartApplicationGenerator()
    };
  }
}
