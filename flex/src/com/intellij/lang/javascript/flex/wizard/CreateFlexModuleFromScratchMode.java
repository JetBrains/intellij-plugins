package com.intellij.lang.javascript.flex.wizard;

import com.intellij.ide.util.newProjectWizard.StepSequence;
import com.intellij.ide.util.newProjectWizard.modes.CreateFromScratchMode;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;

public class CreateFlexModuleFromScratchMode extends CreateFromScratchMode {

  private ModuleBuilder myModuleBuilder;

  protected StepSequence createSteps(final WizardContext context, final ModulesProvider modulesProvider) {
    final StepSequence sequence = new StepSequence(null);
    sequence.addCommonStep(new CreateFlexModuleStep(context, sequence, this));
    return sequence;
  }

  public ModuleBuilder getModuleBuilder() {
    if (myModuleBuilder == null) {
      myModuleBuilder = new FlexIdeModuleBuilder();
    }
    return myModuleBuilder;
  }

  public void dispose() {
    myModuleBuilder = null;
    super.dispose();
  }
}
