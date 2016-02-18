package com.intellij.coldFusion.projectWizard;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.ProjectBuilder;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.options.ConfigurationException;

import javax.swing.*;

/**
 * Created by jetbrains on 11/02/16.
 */
public class CfmlModuleWizardStep extends ModuleWizardStep implements Disposable{
  private final WizardContext myContext;
  private final CfmlGeneratorPeer myPeer;


  public CfmlModuleWizardStep(final WizardContext context) {
    myContext = context;
    myPeer = new CfmlGeneratorPeer();
  }

  @Override
  public JComponent getComponent() {
    return myPeer.getComponent();
  }

  @Override
  public void updateDataModel() {
    final ProjectBuilder projectBuilder = myContext.getProjectBuilder();
    if (projectBuilder instanceof CfmlModuleBuilder) {
      ((CfmlModuleBuilder)projectBuilder).setWizardData(myPeer.getSettings());
    }
  }

  @Override
  public boolean validate() throws ConfigurationException {
    return myPeer.validateInIntelliJ();
  }

  @Override
  public void dispose() {

  }
}
