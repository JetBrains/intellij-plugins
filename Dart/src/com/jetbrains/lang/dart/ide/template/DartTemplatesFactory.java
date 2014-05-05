package com.jetbrains.lang.dart.ide.template;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.platform.ProjectTemplate;
import com.intellij.platform.ProjectTemplatesFactory;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DartTemplatesFactory extends ProjectTemplatesFactory {

  public static final String GROUP_NAME = "Dart";

  @NotNull
  @Override
  public String[] getGroups() {
    return new String[]{ GROUP_NAME };
  }

  @NotNull
  @Override
  public ProjectTemplate[] createTemplates(String group, WizardContext context) {
    return new ProjectTemplate[]{
      new DartWebApplicationGenerator(), new DartCommandLineApplicationGenerator()
    };
  }

  @Override
  public Icon getGroupIcon(final String group) {
    return DartIcons.Dart_24;
  }

}
