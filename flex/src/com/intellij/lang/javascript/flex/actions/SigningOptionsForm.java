package com.intellij.lang.javascript.flex.actions;

import com.intellij.lang.javascript.flex.actions.airinstaller.CertificateParameters;
import com.intellij.lang.javascript.flex.actions.airinstaller.CreateCertificateDialog;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.HoverHyperlinkLabel;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SigningOptionsForm {
  private static final String MORE_OPTIONS = "More options";
  private static final String LESS_OPTIONS = "Less options";

  private JPanel myMainPanel; // required to reuse this form

  private JCheckBox myUseTempCertificateCheckBox;
  private JPanel myOptionsPanel;

  private JLabel myProvisioningProfileLabel;
  private TextFieldWithBrowseButton myProvisioningProfileTextWithBrowse;

  private TextFieldWithBrowseButton myKeystoreFileTextWithBrowse;
  private JComboBox myKeystoreTypeCombo;
  private JPasswordField myKeystorePasswordField;
  private HoverHyperlinkLabel myMoreOptionsHyperlinkLabel;
  private JLabel myKeyAliasLabel;
  private JTextField myKeyAliasTextField;
  private JLabel myKeyPasswordLabel;
  private JPasswordField myKeyPasswordField;
  private JLabel myProviderClassNameLabel;
  private JTextField myProviderClassNameTextField;
  private JLabel myTsaUrlLabel;
  private JTextField myTsaUrlTextField;

  private JButton myCreateCertButton;

  private final Project myProject;
  private final Computable<Module> myModuleComputable;
  private final Computable<Sdk> mySdkComputable;
  private final Runnable myResizeHandler;

  public SigningOptionsForm(final Project project,
                            final Computable<Module> moduleComputable,
                            final Computable<Sdk> sdkComputable,
                            final Runnable resizeHandler) {
    myProject = project;
    myModuleComputable = moduleComputable;
    mySdkComputable = sdkComputable;
    myResizeHandler = resizeHandler;

    initUseTempCertificateCheckBox();

    myProvisioningProfileTextWithBrowse.addBrowseFolderListener(null, null, myProject,
                                                                FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor());
    myKeystoreFileTextWithBrowse
      .addBrowseFolderListener(null, null, myProject, FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor());

    initCreateCertButton();
    initMoreOptionsHyperlinkLabel();
    updateMoreOptions();
  }

  private void initUseTempCertificateCheckBox() {
    myUseTempCertificateCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        updateControls();
      }
    });
  }

  private void initCreateCertButton() {
    myCreateCertButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final Sdk flexSdk = mySdkComputable.compute();
        if (flexSdk == null) {
          Messages.showErrorDialog(myProject, "Flex or AIR SDK is required to create certificate", CreateCertificateDialog.TITLE);
        }
        else {
          final CreateCertificateDialog dialog = new CreateCertificateDialog(myProject, flexSdk, suggestKeystoreFileLocation());
          dialog.show();
          if (dialog.isOK()) {
            final CertificateParameters parameters = dialog.getCertificateParameters();
            myKeystoreFileTextWithBrowse.setText(parameters.getKeystoreFilePath());
            myKeystoreTypeCombo.setSelectedIndex(0);
            myKeystorePasswordField.setText(parameters.getKeystorePassword());
          }
        }
      }
    });
  }

  private String suggestKeystoreFileLocation() {
    final Module module = myModuleComputable.compute();
    if (module != null) {
      final VirtualFile[] contentRoots = ModuleRootManager.getInstance(module).getContentRoots();
      if (contentRoots.length > 0) {
        return contentRoots[0].getPath();
      }
    }
    final VirtualFile baseDir = myProject.getBaseDir();
    return baseDir == null ? "" : baseDir.getPath();
  }


  private void initMoreOptionsHyperlinkLabel() {
    myMoreOptionsHyperlinkLabel.setText(MORE_OPTIONS);
    myMoreOptionsHyperlinkLabel.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          showMoreOptions(!isShowingMoreOptions());
          myResizeHandler.run();
        }
      }
    });
  }

  private void createUIComponents() {
    myMoreOptionsHyperlinkLabel = new HoverHyperlinkLabel(MORE_OPTIONS);
  }

  public JPanel getMainPanel() {
    return myMainPanel;
  }

  private void showMoreOptions(final boolean show) {
    myMoreOptionsHyperlinkLabel.setText(show ? LESS_OPTIONS : MORE_OPTIONS);
    updateMoreOptions();
  }

  private boolean isShowingMoreOptions() {
    return myMoreOptionsHyperlinkLabel.getText().contains(LESS_OPTIONS);
  }

  private void updateMoreOptions() {
    final boolean showingMoreOption = isShowingMoreOptions();

    myKeyAliasLabel.setVisible(showingMoreOption);
    myKeyAliasTextField.setVisible(showingMoreOption);
    myKeyPasswordLabel.setVisible(showingMoreOption);
    myKeyPasswordField.setVisible(showingMoreOption);
    myProviderClassNameLabel.setVisible(showingMoreOption);
    myProviderClassNameTextField.setVisible(showingMoreOption);
    myTsaUrlLabel.setVisible(showingMoreOption);
    myTsaUrlTextField.setVisible(showingMoreOption);
  }

  private void updateControls() {
    UIUtil.setEnabled(myOptionsPanel, !myUseTempCertificateCheckBox.isVisible() || !myUseTempCertificateCheckBox.isSelected(), true);
    if (myMoreOptionsHyperlinkLabel.isEnabled()) {
      myMoreOptionsHyperlinkLabel.setForeground(Color.BLUE); // workaround of JLabel-related workaround at UIUtil.setEnabled(..)
    }
  }

  public void setEnabled(final boolean enabled) {
    UIUtil.setEnabled(myMainPanel, enabled, true);
    if (enabled) {
      updateControls();
    }
  }

  public void setUseTempCertificateCheckBoxVisible(final boolean visible) {
    myUseTempCertificateCheckBox.setVisible(visible);
    updateControls();
  }

  public void setProvisioningProfileApplicable(final boolean applicable) {
    myProvisioningProfileLabel.setVisible(applicable);
    myProvisioningProfileTextWithBrowse.setVisible(applicable);
  }

  public void setCreateCertificateButtonApplicable(final boolean applicable) {
    myCreateCertButton.setVisible(applicable);
  }

  public String getProvisioningProfilePath() {
    return myProvisioningProfileTextWithBrowse.getText().trim();
  }

  public void setProvisioningProfilePath(final String provisioningProfilePath) {
    myProvisioningProfileTextWithBrowse.setText(provisioningProfilePath);
  }

  public String getKeystorePath() {
    return myKeystoreFileTextWithBrowse.getText().trim();
  }

  public void setKeystorePath(final String keystorePath) {
    myKeystoreFileTextWithBrowse.setText(keystorePath);
  }

  public String getKeystoreType() {
    return (String)myKeystoreTypeCombo.getSelectedItem();
  }

  public void setKeystoreType(final String keystoreType) {
    myKeystoreTypeCombo.setSelectedItem(keystoreType);
  }

  public String getKeystorePassword() {
    return new String(myKeystorePasswordField.getPassword());
  }

  public void setKeystorePassword(final String password) {
    myKeystorePasswordField.setText(password);
  }

  public String getKeyAlias() {
    return isShowingMoreOptions() ? myKeyAliasTextField.getText().trim() : "";
  }

  public void setKeyAlias(final String keyAlias) {
    myKeyAliasTextField.setText(keyAlias);
    if (StringUtil.isNotEmpty(keyAlias)) {
      showMoreOptions(true);
    }
  }

  public String getKeyPassword() {
    return isShowingMoreOptions() ? new String(myKeyPasswordField.getPassword()) : "";
  }

  public void setKeyPassword(final String password) {
    myKeyPasswordField.setText(password);
  }

  public String getProviderClassName() {
    return isShowingMoreOptions() ? myProviderClassNameTextField.getText().trim() : "";
  }

  public void setProviderClassName(final String providerClassName) {
    myProviderClassNameTextField.setText(providerClassName);
    if (StringUtil.isNotEmpty(providerClassName)) {
      showMoreOptions(true);
    }
  }

  public String getTsaUrl() {
    return isShowingMoreOptions() ? myTsaUrlTextField.getText().trim() : "";
  }

  public void setTsaUrl(final String tsaUrl) {
    myTsaUrlTextField.setText(tsaUrl);
    if (StringUtil.isNotEmpty(tsaUrl)) {
      showMoreOptions(true);
    }
  }

  public void resetFrom(final AirSigningOptions signingOptions) {
    myUseTempCertificateCheckBox.setSelected(signingOptions.isUseTempCertificate());
    myProvisioningProfileTextWithBrowse.setText(FileUtil.toSystemDependentName(signingOptions.getProvisioningProfilePath()));
    myKeystoreFileTextWithBrowse.setText(FileUtil.toSystemDependentName(signingOptions.getKeystorePath()));
    myKeystoreTypeCombo.setSelectedItem(signingOptions.getKeystoreType());
    myKeystorePasswordField.setText(signingOptions.getKeystorePassword());
    myKeyAliasTextField.setText(signingOptions.getKeyAlias());
    myKeyPasswordField.setText(signingOptions.getKeyPassword());
    myProviderClassNameTextField.setText(signingOptions.getProvider());
    myTsaUrlTextField.setText(signingOptions.getTsa());
    updateControls();
  }

  public boolean isModified(final AirSigningOptions signingOptions) {
    if (myUseTempCertificateCheckBox.isVisible() && myUseTempCertificateCheckBox.isSelected() != signingOptions.isUseTempCertificate()) {
      return true;
    }
    final String provisioningPath = FileUtil.toSystemIndependentName(myProvisioningProfileTextWithBrowse.getText().trim());
    if (myProvisioningProfileTextWithBrowse.isVisible() && !provisioningPath.equals(signingOptions.getProvisioningProfilePath())) {
      return true;
    }
    if (!FileUtil.toSystemIndependentName(myKeystoreFileTextWithBrowse.getText().trim()).equals(signingOptions.getKeystorePath())) {
      return true;
    }
    if (!myKeystoreTypeCombo.getSelectedItem().equals(signingOptions.getKeystoreType())) return true;
    if (!myKeystorePasswordField.getText().equals(signingOptions.getKeystorePassword())) return true;
    if (!myKeyAliasTextField.getText().equals(signingOptions.getKeyAlias())) return true;
    if (!myKeyPasswordField.getText().equals(signingOptions.getKeyPassword())) return true;
    if (!myProviderClassNameTextField.getText().equals(signingOptions.getProvider())) return true;
    if (!myTsaUrlTextField.getText().equals(signingOptions.getTsa())) return true;

    return false;
  }


  public void applyTo(final AirSigningOptions signingOptions) {
    signingOptions.setUseTempCertificate(myUseTempCertificateCheckBox.isSelected());
    signingOptions.setProvisioningProfilePath(FileUtil.toSystemIndependentName(myProvisioningProfileTextWithBrowse.getText().trim()));
    signingOptions.setKeystorePath(FileUtil.toSystemIndependentName(myKeystoreFileTextWithBrowse.getText().trim()));
    signingOptions.setKeystoreType((String)myKeystoreTypeCombo.getSelectedItem());
    signingOptions.setKeystorePassword(new String(myKeystorePasswordField.getPassword()));
    signingOptions.setKeyAlias(myKeyAliasTextField.getText());
    signingOptions.setKeyPassword(new String(myKeyPasswordField.getPassword()));
    signingOptions.setProvider(myProviderClassNameTextField.getText());
    signingOptions.setTsa(myTsaUrlTextField.getText());
  }
}
