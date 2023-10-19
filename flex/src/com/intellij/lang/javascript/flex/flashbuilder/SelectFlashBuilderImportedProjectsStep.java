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

  @Override
  protected String getElementText(final String dotProjectFilePath) {
    return FlashBuilderProjectLoadUtil.readProjectName(dotProjectFilePath) +
           " [" +
           FileUtil.toSystemDependentName(dotProjectFilePath).substring(0, dotProjectFilePath.length() -
                                                                                     (1 + FlashBuilderImporter.DOT_PROJECT).length()) +
           "]";
  }

  @Override
  public boolean isStepVisible() {
    final FlashBuilderImporter builder = (FlashBuilderImporter)getBuilder();
    if (builder == null) return false;
    // no need in this step if one archive file or one FB project was explicitly selected
    final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(builder.getInitiallySelectedPath());

    if (file != null) {
      if (file.isDirectory()) {
        if (FlashBuilderProjectFinder.isFlashBuilderProject(file.findChild(FlashBuilderImporter.DOT_PROJECT))) {
          return false;
        }
      }
      else if (FlashBuilderProjectFinder.isFlashBuilderProject(file) || FlashBuilderProjectFinder.hasArchiveExtension(file.getPath())) {
        return false;
      }
    }

    return super.isStepVisible();
  }

  @Override
  public String getHelpId() {
    return "reference.dialogs.new.project.import.flex.page2";
  }
}
