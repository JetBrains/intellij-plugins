package com.intellij.lang.javascript.flex.flashbuilder;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.projectImport.SelectImportedProjectsStep;

public class SelectFlashBuilderImportedProjectsStep extends SelectImportedProjectsStep<String> {
  public SelectFlashBuilderImportedProjectsStep(final WizardContext context) {
    super(context);
  }

  protected String getElementText(final String dotProjectFilePath) {
    final StringBuilder builder = new StringBuilder();
    builder.append(FlashBuilderProjectLoadUtil.getProjectName(dotProjectFilePath));
    builder.append(" [");
    builder.append(FileUtil.toSystemDependentName(dotProjectFilePath).substring(0, dotProjectFilePath.length() -
                                                                                   (1 + FlashBuilderImporter.DOT_PROJECT).length()));
    builder.append("]");
    return builder.toString();
  }

  public String getHelpId() {
    return "reference.dialogs.new.project.import.flex.page2";
  }
}
