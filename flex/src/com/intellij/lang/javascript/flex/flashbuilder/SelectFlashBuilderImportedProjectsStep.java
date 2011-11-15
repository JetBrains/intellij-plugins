package com.intellij.lang.javascript.flex.flashbuilder;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.projectImport.SelectImportedProjectsStep;

public class SelectFlashBuilderImportedProjectsStep extends SelectImportedProjectsStep<String> {
  public SelectFlashBuilderImportedProjectsStep(final WizardContext context) {
    super(context);
  }

  protected String getElementText(final String dotProjectFilePath) {
    final StringBuilder builder = new StringBuilder();
    builder.append(FlashBuilderProjectLoadUtil.readProjectName(dotProjectFilePath));
    builder.append(" [");
    builder.append(FileUtil.toSystemDependentName(dotProjectFilePath).substring(0, dotProjectFilePath.length() -
                                                                                   (1 + FlashBuilderImporter.DOT_PROJECT).length()));
    builder.append("]");
    return builder.toString();
  }

  public boolean isStepVisible() {
    // no need in this step if one fxp file or one FB project was explicitly selected
    final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(((FlashBuilderImporter)getBuilder()).getInitiallySelectedPath());

    if (file != null) {
      if (file.isDirectory()) {
        if (FlashBuilderProjectFinder.isFlashBuilderProject(file.findChild(FlashBuilderImporter.DOT_PROJECT))) {
          return false;
        }
      }
      else if (FlashBuilderProjectFinder.isFlashBuilderProject(file) || FlashBuilderProjectFinder.isArchivedFBProject(file.getPath())) {
        return false;
      }
    }

    return super.isStepVisible();
  }

  public String getHelpId() {
    return "reference.dialogs.new.project.import.flex.page2";
  }
}
