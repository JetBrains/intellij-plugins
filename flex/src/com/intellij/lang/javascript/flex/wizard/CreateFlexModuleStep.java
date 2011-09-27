package com.intellij.lang.javascript.flex.wizard;

import com.intellij.ide.util.newProjectWizard.ProjectNameWithTypeStep;
import com.intellij.ide.util.newProjectWizard.StepSequence;
import com.intellij.ide.util.newProjectWizard.modes.WizardMode;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.DocumentAdapter;

import javax.swing.event.DocumentEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CreateFlexModuleStep extends ProjectNameWithTypeStep {

  private final FlexIdeModuleWizardForm myForm;

  public CreateFlexModuleStep(final WizardContext wizardContext, final StepSequence sequence, final WizardMode mode) {
    super(wizardContext, sequence, mode);

    myForm = new FlexIdeModuleWizardForm();
    myForm.reset(getModuleName());

    replaceModuleTypeOptions(myForm.getMainPanel());

    addModuleNameListener(new DocumentAdapter() {
      protected void textChanged(final DocumentEvent e) {
        myForm.onModuleNameChanged(getModuleName());
      }
    });

    myCreateModuleCb.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        updateControls();
      }
    });
  }

  private void updateControls() {
    myForm.setEnabled(myCreateModuleCb.isSelected());
  }

  protected String getSelectedBuilderId() {
    return FlexModuleType.getInstance().getId();
  }

  public void updateStep() {
    super.updateStep();
    updateControls();
  }

  public void updateDataModel() {
    super.updateDataModel();
    if (myCreateModuleCb.isSelected()) {
      myForm.applyTo((FlexIdeModuleBuilder)myMode.getModuleBuilder());
    }
  }

  public boolean validate() throws ConfigurationException {
    final boolean ok = super.validate();
    if (!ok) {
      return false;
    }

    return myForm.validate();
  }

  public void disposeUIResources() {
    myForm.disposeUIResources();
    super.disposeUIResources();
  }
}
