package com.intellij.lang.javascript.flex.flashbuilder;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.projectImport.ProjectOpenProcessorBase;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collections;

public class FlashBuilderOpenProcessor extends ProjectOpenProcessorBase<FlashBuilderImporter> {

  public FlashBuilderOpenProcessor(@NotNull final FlashBuilderImporter builder) {
    super(builder);
  }

  public String[] getSupportedExtensions() {
    return new String[]{FlashBuilderImporter.DOT_PROJECT, FlashBuilderImporter.DOT_FXP, FlashBuilderImporter.DOT_FXPL};
  }

  public boolean canOpenProject(final VirtualFile file) {
    return file.isDirectory()
           ? FlashBuilderProjectFinder.isFlashBuilderProject(file.findChild(FlashBuilderImporter.DOT_PROJECT))
           : FlashBuilderProjectFinder.isFlashBuilderProject(file) || FlashBuilderProjectFinder.isArchivedFBProject(file.getPath());
  }

  protected boolean doQuickImport(final VirtualFile file, final WizardContext wizardContext) {
    final String title = FlexBundle.message("open.project.0", file.getName());

    if (FlashBuilderProjectFinder.isArchivedFBProject(file.getPath())) {
      try {
        FlashBuilderProjectFinder.checkFxpFile(file.getPath());
      }
      catch (ConfigurationException e) {
        Messages.showErrorDialog(wizardContext.getProject(), e.getMessage(), title);
        return false;
      }

      final String label = FlashBuilderProjectFinder.isMultiProjectFxp(file.getPath())
                           ? FlexBundle.message("folder.to.unzip.several.FB.projects")
                           : FlexBundle.message("folder.to.unzip.one.FB.project");
      final String path = FileUtil.toSystemDependentName(wizardContext.getProjectFileDirectory() + "/" + file.getNameWithoutExtension());
      final AskPathDialog dialog = new AskPathDialog(title, label, path);

      dialog.show();

      if (!dialog.isOK()) {
        return false;
      }

      final File projectDir = new File(dialog.getPath());
      if (!projectDir.exists() && !projectDir.mkdirs()) {
        Messages.showErrorDialog("Unable to create folder: " + dialog.getPath(), title);
        return false;
      }

      wizardContext.setProjectFileDirectory(dialog.getPath());
    }
    getBuilder().setInitiallySelectedPath(file.getPath());
    getBuilder().setList(Collections.singletonList(file.getPath()));
    wizardContext.setProjectName(getBuilder().getSuggestedProjectName());
    return true;
  }
}
