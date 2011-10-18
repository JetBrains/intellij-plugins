package com.intellij.lang.javascript.flex.flashbuilder;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.ProjectWizardStepFactory;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.projectImport.ProjectImportProvider;
import com.intellij.util.PlatformUtils;

public class FlashBuilderImportProvider extends ProjectImportProvider {

  public FlashBuilderImportProvider(final FlashBuilderImporter builder) {
    super(builder);
  }

  public ModuleWizardStep[] createSteps(final WizardContext context) {
    final ProjectWizardStepFactory stepFactory = ProjectWizardStepFactory.getInstance();
    if (PlatformUtils.isFlexIde()) {
      return new ModuleWizardStep[]{new SelectDirWithFlashBuilderProjectsStep(context), new SelectFlashBuilderImportedProjectsStep(context),
        stepFactory.createNameAndLocationStep(context)};
    }
    else {
      return new ModuleWizardStep[]{new SelectDirWithFlashBuilderProjectsStep(context), new SelectFlashBuilderImportedProjectsStep(context),
        stepFactory.createProjectJdkStep(context), stepFactory.createNameAndLocationStep(context)};
    }
  }
}
