package com.google.jstestdriver.idea.assertFramework.support;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

public class AdapterSupportDialog extends DialogWrapper {

  private final String myAssertFrameworkName;
  private List<VirtualFile> myAdapterSourceFiles;
  private JPanel myContent;

  private JPanel myDirectoryTypeContent;
  private JRadioButton myDefaultRadioButton;
  private JRadioButton myCustomRadioButton;
  private JPanel myLibraryNameDefinitionPanel;
  private JTextField myNewLibraryNameTextField;

  private final DirectoryTypeManager myDirectoryTypeManager;
  private final NewLibraryCreationManager myNewLibraryCreationManager;

  public AdapterSupportDialog(@NotNull Project project,
                              @NotNull String assertionFrameworkName,
                              @NotNull List<VirtualFile> adapterSourceFiles) {
    super(project);
    myAssertFrameworkName = assertionFrameworkName;
    myAdapterSourceFiles = adapterSourceFiles;

    setModal(true);
    updateAssertionFrameworkSpecificDescriptions();
    myDirectoryTypeManager = DirectoryTypeManager.install(project, myDirectoryTypeContent, assertionFrameworkName, myDefaultRadioButton, myCustomRadioButton);
    myNewLibraryCreationManager = NewLibraryCreationManager.install(project, myLibraryNameDefinitionPanel, myNewLibraryNameTextField, assertionFrameworkName);
    myDirectoryTypeManager.addChangeListener(myNewLibraryCreationManager);
    myDirectoryTypeManager.init();
    init();
  }

  private String getAssertFrameworkNameAdapter() {
    return myAssertFrameworkName + " adapter";
  }

  private void updateAssertionFrameworkSpecificDescriptions() {
    setTitle("Add " + getAssertFrameworkNameAdapter() + " support for JsTestDriver");
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
    return new Action[] { getOKAction(), getCancelAction() };
  }

}
