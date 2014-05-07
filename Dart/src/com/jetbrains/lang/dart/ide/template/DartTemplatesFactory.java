package com.jetbrains.lang.dart.ide.template;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.platform.ProjectTemplate;
import com.intellij.platform.ProjectTemplatesFactory;
import com.jetbrains.lang.dart.DartBundle;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DartTemplatesFactory extends ProjectTemplatesFactory {

  public static final String GROUP_NAME = "Dart";

  // Generator with labels tailored for IDEA
  private static final DartWebApplicationGenerator WEB_APP_GENERATOR = new DartWebApplicationGenerator() {
    @NotNull
    @Override
    public String getDescription() {
      return super.getDescription() + " " + DartBundle.message("dart.dartlang.href");
    }

    @NotNull
    @Override
    public String getName() {
      return DartBundle.message("dart.web.application.title");
    }
  };

  // Generator with labels tailored for IDEA
  private static final DartCommandLineApplicationGenerator CMD_LINE_APP_GENERATOR = new DartCommandLineApplicationGenerator() {
    @NotNull
    @Override
    public String getDescription() {
      return super.getDescription() + " " + DartBundle.message("dart.dartlang.href");
    }

    @NotNull
    @Override
    public String getName() {
      return DartBundle.message("dart.commandline.application.title");
    }
  };


  @NotNull
  @Override
  public String[] getGroups() {
    return new String[]{ GROUP_NAME };
  }

  @NotNull
  @Override
  public ProjectTemplate[] createTemplates(String group, WizardContext context) {
    return new ProjectTemplate[]{ WEB_APP_GENERATOR, CMD_LINE_APP_GENERATOR };
  }

  @Override
  public Icon getGroupIcon(final String group) {
    return DartIcons.Dart_16;
  }

}
