package com.jetbrains.lang.dart.ide.template;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.platform.ProjectTemplate;
import com.intellij.platform.ProjectTemplatesFactory;
import com.jetbrains.lang.dart.ide.module.DartModuleType;
import org.jetbrains.annotations.NotNull;

public class DartTemplatesFactory extends ProjectTemplatesFactory {

  @NotNull
  @Override
  public String[] getGroups() {
    return new String[]{ DartModuleType.GROUP_NAME };
  }

  @NotNull
  @Override
  public ProjectTemplate[] createTemplates(String group, WizardContext context) {
    return new ProjectTemplate[]{
      new DartWebApplicationGenerator(), new DartCommandLineApplicationGenerator()
    };
  }
}
