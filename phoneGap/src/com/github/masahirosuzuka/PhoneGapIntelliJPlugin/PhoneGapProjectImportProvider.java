package com.github.masahirosuzuka.PhoneGapIntelliJPlugin;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ui.SelectPhoneGapImportModuleStep;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.projectImport.ProjectImportProvider;

/**
 * Created by Masahiro Suzuka on 2014/04/08.
 */
public class PhoneGapProjectImportProvider extends ProjectImportProvider {

  public PhoneGapProjectImportProvider(final PhoneGapProjectImportBuilder builder) {
    super(builder);
  }

  public ModuleWizardStep[] createSteps(WizardContext context) {
    //final ProjectWizardStepFactory stepFactory = ProjectWizardStepFactory.getInstance();
    return new ModuleWizardStep[]{
      new SelectPhoneGapImportModuleStep(context)
    };
  }
}
