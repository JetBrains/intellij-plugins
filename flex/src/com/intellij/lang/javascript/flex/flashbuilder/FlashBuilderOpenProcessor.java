package com.intellij.lang.javascript.flex.flashbuilder;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.projectImport.ProjectOpenProcessorBase;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class FlashBuilderOpenProcessor extends ProjectOpenProcessorBase<FlashBuilderImporter> {

  public FlashBuilderOpenProcessor(@NotNull final FlashBuilderImporter builder) {
    super(builder);
  }

  public String[] getSupportedExtensions() {
    return new String[]{FlashBuilderImporter.DOT_PROJECT};
  }

  public boolean canOpenProject(final VirtualFile file) {
    if (super.canOpenProject(file)) {
      if (file.isDirectory()) {
        final String[] supported = getSupportedExtensions();
        for (VirtualFile child : file.getChildren()) {
          if (canOpenFile(child, supported)) return FlashBuilderProjectFinder.isFlashBuilderProject(child);
        }
      }
      return FlashBuilderProjectFinder.isFlashBuilderProject(file);
    }
    return false;
  }

  protected boolean doQuickImport(final VirtualFile file, final WizardContext wizardContext) {
    getBuilder().setInitiallySelectedDirPath(file.getParent().getPath());
    getBuilder().setList(Collections.singletonList(file.getPath()));
    wizardContext.setProjectName(getBuilder().getSuggestedProjectName());
    return true;
  }
}
