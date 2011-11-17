package com.intellij.lang.javascript.flex.projectStructure.detection;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.javascript.flex.FlexSupportProvider;
import com.intellij.lang.javascript.flex.sdk.FlexSdkComboBoxWithBrowseButton;

import javax.swing.*;

public class FlexSdkStep extends ModuleWizardStep {
  private JPanel myContentPane;
  private FlexSdkComboBoxWithBrowseButton mySdkCombo;
  private WizardContext myContext;

  public FlexSdkStep(WizardContext context) {
    myContext = context;
    FlexSupportProvider.selectSdkUsedByOtherModules(myContext.getProject(), mySdkCombo);
  }

  public JComponent getComponent() {
    return myContentPane;
  }

  public void updateDataModel() {
    myContext.setProjectJdk(mySdkCombo.getSelectedSdk());
  }
}
