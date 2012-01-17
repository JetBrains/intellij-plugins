package com.intellij.lang.javascript.flex;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.lang.javascript.flex.wizard.FlexIdeModuleWizardForm;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;

import javax.swing.*;

/**
 * User: ksafonov
 */
public class SetupFlexModuleWizardStep extends ModuleWizardStep {

  FlexIdeModuleWizardForm myForm;
  private FlexModuleBuilder myModuleBuilder;

  public SetupFlexModuleWizardStep(final FlexModuleBuilder moduleBuilder) {
    myModuleBuilder = moduleBuilder;
    myForm = new FlexIdeModuleWizardForm();
  }

  public JComponent getComponent() {
    return myForm.getMainPanel();
  }

  public void _init() {
    myForm.reset(myModuleBuilder.getName());
  }

  public void updateDataModel() {
    myForm.applyTo(myModuleBuilder);
  }
}
