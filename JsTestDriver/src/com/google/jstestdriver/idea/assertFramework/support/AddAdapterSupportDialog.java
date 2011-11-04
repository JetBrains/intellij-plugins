package com.google.jstestdriver.idea.assertFramework.support;

import com.google.jstestdriver.idea.util.CastUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.util.List;

public class AddAdapterSupportDialog extends DialogWrapper {

  private final String myAssertFrameworkName;
  private List<VirtualFile> myAdapterSourceFiles;
  private JPanel myContent;

  private JPanel myDirectoryTypeContent;
  private JRadioButton myDefaultRadioButton;
  private JRadioButton myCustomRadioButton;
  private JTextField myNewLibraryNameTextField;
  private JPanel myTestsRunPanel;
  private JPanel myCodeAssistancePanel;

  private final ExtractDirectoryTypeManager myDirectoryTypeManager;
  private final NewLibraryCreationManager myNewLibraryCreationManager;

  public AddAdapterSupportDialog(@NotNull Project project,
                                 @NotNull String assertionFrameworkName,
                                 @NotNull List<VirtualFile> adapterSourceFiles) {
    super(project);
    myAssertFrameworkName = assertionFrameworkName;
    myAdapterSourceFiles = adapterSourceFiles;

    setModal(true);
    updateAssertionFrameworkSpecificDescriptions();
    myDirectoryTypeManager = ExtractDirectoryTypeManager.install(
      project, myDirectoryTypeContent, assertionFrameworkName, myDefaultRadioButton, myCustomRadioButton
    );
    myNewLibraryCreationManager = NewLibraryCreationManager.install(
      project, myNewLibraryNameTextField, assertionFrameworkName
    );
    myDirectoryTypeManager.addChangeListener(myNewLibraryCreationManager);
    myDirectoryTypeManager.init();
    init();
  }

  private String getAssertFrameworkAdapterName() {
    return myAssertFrameworkName + " adapter";
  }

  private void updateAssertionFrameworkSpecificDescriptions() {
    setTitle("Add " + getAssertFrameworkAdapterName() + " support for JsTestDriver");
    TitledBorder testsRunPanelTitledBorder = CastUtils.tryCast(myTestsRunPanel.getBorder(), TitledBorder.class);
    if (testsRunPanelTitledBorder != null) {
      String title = String.format(testsRunPanelTitledBorder.getTitle(), myAssertFrameworkName);
      testsRunPanelTitledBorder.setTitle(title);
    }
    TitledBorder codeAssistancePanelTitledBorder = CastUtils.tryCast(
      myCodeAssistancePanel.getBorder(), TitledBorder.class
    );
    if (codeAssistancePanelTitledBorder != null) {
      String title = String.format(codeAssistancePanelTitledBorder.getTitle(), myAssertFrameworkName);
      codeAssistancePanelTitledBorder.setTitle(title);
    }
  }

  @Override
  protected JComponent createCenterPanel() {
    return myContent;
  }

  @Override
  protected ValidationInfo doValidate() {
    ValidationInfo validationInfo = myDirectoryTypeManager.validate();
    if (validationInfo != null) {
      return validationInfo;
    }
    validationInfo = myNewLibraryCreationManager.validate();
    return validationInfo;
  }

  @Override
  protected void doOKAction() {
    List<VirtualFile> extractedVirtualFiles = myDirectoryTypeManager.extractAdapterFiles(myAdapterSourceFiles);
    if (extractedVirtualFiles != null) {
      myNewLibraryCreationManager.installCodeAssistance(extractedVirtualFiles);
    }
    super.doOKAction();
  }

  @Override
  protected Action[] createActions() {
    Action okAction = getOKAction();
    okAction.putValue(Action.NAME, "Install");
    return new Action[] { okAction, getCancelAction() };
  }

}
