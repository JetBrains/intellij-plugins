package com.jetbrains.lang.dart.projectWizard;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.WebModuleBuilder;
import com.intellij.platform.ProjectTemplate;
import com.intellij.platform.ProjectTemplatesFactory;
import com.jetbrains.lang.dart.DartBundle;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DartTemplatesFactory extends ProjectTemplatesFactory {

  private static final String GROUP_NAME = "Dart";

  @NotNull
  @Override
  public String[] getGroups() {
    return new String[]{GROUP_NAME};
  }

  /*
  @Override
  public String getParentGroup(String group) {
    return WebModuleBuilder.GROUP_NAME;
  }
  */

  @NotNull
  @Override
  public ProjectTemplate[] createTemplates(String group, WizardContext context) {
    return new ProjectTemplate[]{
      new DartEmptyProjectGenerator(DartBundle.message("empty.dart.project.description.idea")),
      new DartWebAppGenerator(DartBundle.message("dart.web.app.description.idea")),
      new DartCmdLineAppGenerator(DartBundle.message("dart.commandline.app.description.idea"))
    };
  }

  @Override
  public Icon getGroupIcon(final String group) {
    return DartIcons.Dart_16;
  }
}
