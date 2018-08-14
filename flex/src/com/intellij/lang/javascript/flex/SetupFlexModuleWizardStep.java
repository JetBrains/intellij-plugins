package com.intellij.lang.javascript.flex;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.lang.javascript.flex.wizard.FlexModuleWizardForm;
import com.intellij.openapi.options.ConfigurationException;

import javax.swing.*;

public class SetupFlexModuleWizardStep extends ModuleWizardStep {

  private final FlexModuleWizardForm myForm;
  private final FlexModuleBuilder myModuleBuilder;

  public SetupFlexModuleWizardStep(final FlexModuleBuilder moduleBuilder) {
    myModuleBuilder = moduleBuilder;
    myForm = new FlexModuleWizardForm();
  }

  @Override
  public JComponent getComponent() {
    return myForm.getMainPanel();
  }

  @Override
  public void updateDataModel() {
    myForm.applyTo(myModuleBuilder);
  }

  @Override
  public boolean validate() throws ConfigurationException {
    return myForm.validate();
  }

  @Override
  public String getHelpId() {
    return "reference.dialogs.new.project.fromScratch.flex";
  }
}
