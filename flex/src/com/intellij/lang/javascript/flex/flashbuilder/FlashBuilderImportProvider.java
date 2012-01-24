package com.intellij.lang.javascript.flex.flashbuilder;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.projectImport.ProjectImportProvider;

public class FlashBuilderImportProvider extends ProjectImportProvider {

  public FlashBuilderImportProvider(final FlashBuilderImporter builder) {
    super(builder);
  }

  public ModuleWizardStep[] createSteps(final WizardContext context) {
    return new ModuleWizardStep[]{new SelectDirWithFlashBuilderProjectsStep(context), new SelectFlashBuilderImportedProjectsStep(context)};
  }
}
