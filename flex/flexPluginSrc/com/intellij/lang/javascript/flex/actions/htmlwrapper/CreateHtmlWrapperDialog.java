package com.intellij.lang.javascript.flex.actions.htmlwrapper;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.DialogWrapper;

import javax.swing.*;

public class CreateHtmlWrapperDialog extends DialogWrapper {

  private final CreateHtmlWrapperForm myCreateHtmlWrapperForm;
 
  public CreateHtmlWrapperDialog(final Project project) {
    super(project, true);
    setTitle("Create HTML Wrapper for Flex Application");
    setOKButtonText("Create");
    myCreateHtmlWrapperForm = new CreateHtmlWrapperForm(project);
    myCreateHtmlWrapperForm.addListener(new CreateHtmlWrapperForm.Listener() {
      public void stateChanged() {
        updateControls();
      }
    });
    init();
    updateControls();
  }

  public void setModuleAndSdkAndWrapperLocation(final Module module, final Sdk flexSdk, final String htmlWrapperFileLocation) {
    myCreateHtmlWrapperForm.setModuleAndSdkAndWrapperLocation(module, flexSdk, htmlWrapperFileLocation);
  }

  private void updateControls() {
    setOKActionEnabled(myCreateHtmlWrapperForm.getHTMLWrapperParameters() != null);
    setErrorText(myCreateHtmlWrapperForm.getCurrentErrorMessage());
  }

  protected JComponent createCenterPanel() {
    return myCreateHtmlWrapperForm.getMainPanel();
  }


  public HTMLWrapperParameters getHTMLWrapperParameters() {
    return myCreateHtmlWrapperForm.getHTMLWrapperParameters();
  }

  protected String getHelpId() {
    return "reference.flex.create.html.wrapper";
  }

  public void suggestToCreateRunConfiguration(final boolean b) {
    myCreateHtmlWrapperForm.suggestToCreateRunConfiguration(b);
  }

  public boolean isCreateRunConfigurationSelected(){
    return myCreateHtmlWrapperForm.isCreateRunConfigurationSelected();
  }
}
