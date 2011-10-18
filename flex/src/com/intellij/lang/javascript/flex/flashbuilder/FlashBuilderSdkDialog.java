package com.intellij.lang.javascript.flex.flashbuilder;

import com.intellij.CommonBundle;
import com.intellij.ide.IdeBundle;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.projectStructure.FlexSdk;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.lang.javascript.flex.projectStructure.ui.FlexSdkPanel;
import com.intellij.lang.javascript.flex.sdk.FlexSdkComboBoxWithBrowseButton;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.util.PlatformUtils;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FlashBuilderSdkDialog extends DialogWrapper {

  private final Project myProject;
  private JPanel myMainPanel;
  private JRadioButton myImportSdkSettingsRadioButton;
  private LabeledComponent<TextFieldWithBrowseButton> myWorkspaceDirectoryComponent;
  private LabeledComponent<TextFieldWithBrowseButton> myInstallationDirComponent;
  private JRadioButton myUseIdeaSdkRadioButton;
  private FlexSdkComboBoxWithBrowseButton myFlexSdkComboWithBrowse;
  private JPanel myFlexSdkPanelHolder;
  private FlexSdkPanel myFlexSdkPanel;

  public FlashBuilderSdkDialog(final Project project,
                               final @Nullable FlexProjectConfigurationEditor flexConfigEditor,
                               final boolean chooseWorkspace) {
    super(project);
    myProject = project;

    assert PlatformUtils.isFlexIde() == (flexConfigEditor != null);
    if (PlatformUtils.isFlexIde()) {
      myFlexSdkPanel = new FlexSdkPanel(flexConfigEditor);
      Disposer.register(myDisposable, myFlexSdkPanel);
      myFlexSdkPanel.setSdkLabelVisible(false);
      myFlexSdkPanel.setEditButtonVisible(false);
      myFlexSdkPanel.reset();
      myFlexSdkPanel.setNotNullCurrentSdkIfPossible();

      myFlexSdkPanelHolder.add(myFlexSdkPanel.getContentPane(), BorderLayout.CENTER);
      myFlexSdkComboWithBrowse.setVisible(false);
    }

    myImportSdkSettingsRadioButton.setSelected(true);
    myWorkspaceDirectoryComponent.setVisible(chooseWorkspace);
    myInstallationDirComponent.setVisible(!chooseWorkspace);

    setTitle(IdeBundle.message("title.open.project"));

    setupListeners();
    setupFlashBuilderWorkspaceChooser();
    setupFlashBuilderInstallationDirChooser();

    init();
    updateControls();
  }

  private void setupListeners() {
    myImportSdkSettingsRadioButton.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        updateControls();
        final JTextField toFocus = myWorkspaceDirectoryComponent.isVisible()
                                   ? myWorkspaceDirectoryComponent.getComponent().getTextField()
                                   : myInstallationDirComponent.getComponent().getTextField();
        IdeFocusManager.findInstanceByComponent(myMainPanel).requestFocus(toFocus, true);
      }
    });

    myUseIdeaSdkRadioButton.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        updateControls();
        final JComponent toFocus = myFlexSdkComboWithBrowse.isVisible() ? myFlexSdkComboWithBrowse : myFlexSdkPanel.getCombo();
        IdeFocusManager.findInstanceByComponent(myMainPanel).requestFocus(toFocus, true);
      }
    });
  }

  private void setupFlashBuilderWorkspaceChooser() {
    final String title = FlexBundle.message("select.flash.builder.workspace");
    final FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false) {
      public boolean isFileSelectable(VirtualFile file) {
        return FlashBuilderProjectFinder.isFlashBuilderWorkspace(file);
      }
    };
    myWorkspaceDirectoryComponent.getComponent().addBrowseFolderListener(title, null, myProject, descriptor);
  }

  private void setupFlashBuilderInstallationDirChooser() {
    final String title = FlexBundle.message("select.flash.builder.installation.dir");
    final FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false) {
      public boolean isFileSelectable(VirtualFile file) {
        return FlashBuilderProjectFinder.isFlashBuilderInstallationDir(file);
      }
    };
    myInstallationDirComponent.getComponent().addBrowseFolderListener(title, null, myProject, descriptor);
  }

  public void updateControls() {
    myWorkspaceDirectoryComponent.setEnabled(myImportSdkSettingsRadioButton.isSelected());
    myInstallationDirComponent.setEnabled(myImportSdkSettingsRadioButton.isSelected());

    myFlexSdkComboWithBrowse.setEnabled(myUseIdeaSdkRadioButton.isSelected());
    UIUtil.setEnabled(myFlexSdkPanelHolder, myUseIdeaSdkRadioButton.isSelected(), true);
  }

  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  protected void doOKAction() {
    final String errorMessage = getErrorMessage();
    if (errorMessage == null) {
      super.doOKAction();
    }
    else {
      Messages.showErrorDialog(myProject, errorMessage, CommonBundle.getErrorTitle());
    }
  }


  public boolean isImportFlashBuilderSdkSettings() {
    return myImportSdkSettingsRadioButton.isSelected();
  }

  public String getWorkspacePath() {
    return myWorkspaceDirectoryComponent.getComponent().getText().trim();
  }

  public String getFBInstallationPath() {
    return myInstallationDirComponent.getComponent().getText().trim();
  }

  public boolean isUseIdeaSdkSelected() {
    return myUseIdeaSdkRadioButton.isSelected();
  }

  @Nullable
  public Sdk getSdk() {
    assert !PlatformUtils.isFlexIde();
    return myFlexSdkComboWithBrowse.getSelectedSdk();
  }

  @Nullable
  public String getSdkHome() {
    assert PlatformUtils.isFlexIde();
    final FlexSdk sdk = myFlexSdkPanel.getCurrentSdk();
    return sdk == null ? null : sdk.getHomePath();
  }

  @Nullable
  private String getErrorMessage() {
    if (myImportSdkSettingsRadioButton.isSelected()) {
      if (myWorkspaceDirectoryComponent.isVisible()) {
        final String workspacePath = getWorkspacePath();
        if (workspacePath.length() == 0) {
          return FlexBundle.message("specify.flash.builder.workspace");
        }
        final VirtualFile workspaceDir = LocalFileSystem.getInstance().findFileByPath(workspacePath);
        if (workspaceDir == null || !workspaceDir.isDirectory()) {
          return FlexBundle.message("folder.does.not.exist", workspacePath);
        }
        if (!FlashBuilderProjectFinder.isFlashBuilderWorkspace(workspaceDir)) {
          return FlexBundle.message("not.flash.builder.workspace", workspacePath);
        }
      }
      else {
        final String installationPath = getFBInstallationPath();
        if (installationPath.length() == 0) {
          return FlexBundle.message("specify.flash.builder.installation.dir");
        }
        final VirtualFile workspaceDir = LocalFileSystem.getInstance().findFileByPath(installationPath);
        if (workspaceDir == null || !workspaceDir.isDirectory()) {
          return FlexBundle.message("folder.does.not.exist", installationPath);
        }
        if (!FlashBuilderProjectFinder.isFlashBuilderInstallationDir(workspaceDir)) {
          return FlexBundle.message("not.flash.builder.installation.dir", installationPath);
        }
      }
    }

    return null;
  }
}
