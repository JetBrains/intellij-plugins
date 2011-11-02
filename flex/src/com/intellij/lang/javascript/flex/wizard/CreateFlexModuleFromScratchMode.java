package com.intellij.lang.javascript.flex.wizard;

import com.intellij.ide.util.newProjectWizard.StepSequence;
import com.intellij.ide.util.newProjectWizard.modes.CreateFromScratchMode;
import com.intellij.ide.util.projectWizard.EmptyModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import org.jetbrains.annotations.Nullable;

public class CreateFlexModuleFromScratchMode extends CreateFromScratchMode {

  private CreateFlexModuleStep myCreateFlexModuleStep;
  private ModuleBuilder myFlexIdeModuleBuilder;
  private ModuleBuilder myEmptyModuleBuilder;

  protected StepSequence createSteps(final WizardContext context, @Nullable final ModulesProvider modulesProvider) {
    final StepSequence sequence = new StepSequence();
    myCreateFlexModuleStep = new CreateFlexModuleStep(context, sequence, this);
    sequence.addCommonStep(myCreateFlexModuleStep);
    return sequence;
  }

  public ModuleBuilder getModuleBuilder() {
    if (myCreateFlexModuleStep.isCreateModule()) {
      if (myFlexIdeModuleBuilder == null) {
        myFlexIdeModuleBuilder = new FlexIdeModuleBuilder();
      }
      return myFlexIdeModuleBuilder;
    }
    else {
      if (myEmptyModuleBuilder == null) {
        myEmptyModuleBuilder = new EmptyModuleBuilder();
      }
      return myEmptyModuleBuilder;
    }
  }

  public void dispose() {
    myFlexIdeModuleBuilder = null;
    super.dispose();
  }
}
