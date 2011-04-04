package com.intellij.lang.javascript.flex.actions.airinstaller;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.HoverHyperlinkLabel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.util.Arrays;

public class CreateCertificateDialog extends DialogWrapper {
  private JPanel myMainPanel;
  private JTextField myKeystoreFileNameTextField;
  private TextFieldWithBrowseButton myKeystoreFileLocationTextWithBrowse;
  private JTextField myCertificateNameTextField;
  private JComboBox myKeyTypeCombo;
  private JPasswordField myPasswordField;
  private JPasswordField myConfirmPasswordField;
  private HoverHyperlinkLabel myOptionalParametersHyperlinkLabel;
  private JLabel myOrgUnitLabel;
  private JTextField myOrgUnitTextField;
  private JLabel myOrgNameLabel;
  private JTextField myOrgNameTextField;
  private JLabel myCountryCodeLabel;
  private JTextField myCountryCodeTextField;

  static final String TITLE = "Create Certificate";
  private static final String SHOW_OPTIONAL_PARAMETERS = "Show optional parameters";
  private static final String HIDE_OPTIONAL_PARAMETERS = "Hide optional parameters";
  private final Project myProject;
  private final Sdk myFlexSdk;

  protected CreateCertificateDialog(final Project project, final Sdk flexSdk, final String suggestedKeystoreFileLocation) {
    super(project);
    myProject = project;
    myFlexSdk = flexSdk;

    setTitle(TITLE);
    setOKButtonText("Create");
    initDefaultValues(suggestedKeystoreFileLocation);
    updateControls();

    myKeystoreFileLocationTextWithBrowse
      .addBrowseFolderListener(null, null, project, new FileChooserDescriptor(false, true, false, false, false, false));

    myOptionalParametersHyperlinkLabel.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          myOptionalParametersHyperlinkLabel.setText(isShowingOptionalParameters() ? SHOW_OPTIONAL_PARAMETERS : HIDE_OPTIONAL_PARAMETERS);
          updateControls();
        }
      }
    });

    init();
    IdeFocusManager.getInstance(project).requestFocus(myKeystoreFileNameTextField, true);
  }

  private boolean isShowingOptionalParameters() {
    return myOptionalParametersHyperlinkLabel.getText().contains(HIDE_OPTIONAL_PARAMETERS);
  }

  private void updateControls() {
    final boolean showingOptional = isShowingOptionalParameters();
    myOrgUnitLabel.setVisible(showingOptional);
    myOrgUnitTextField.setVisible(showingOptional);
    myOrgNameLabel.setVisible(showingOptional);
    myOrgNameTextField.setVisible(showingOptional);
    myCountryCodeLabel.setVisible(showingOptional);
    myCountryCodeTextField.setVisible(showingOptional);

    int minHeight = getContentPane().getMinimumSize().height;
    getPeer().setSize(getPeer().getSize().width, minHeight);
  }

  private void initDefaultValues(String suggestedKeystoreFileLocation) {
    myKeystoreFileNameTextField.setText("mykeystore.p12");
    myKeystoreFileLocationTextWithBrowse.setText(FileUtil.toSystemDependentName(suggestedKeystoreFileLocation));
    myCertificateNameTextField.setText("MyCertificate");
    myOptionalParametersHyperlinkLabel.setText(SHOW_OPTIONAL_PARAMETERS);
  }

  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  protected void doOKAction() {
    if (validateInput() && PackageAirInstallerAction.createCertificate(myProject, myFlexSdk, getCertificateParameters())) {
      super.doOKAction();
    }
  }

  private boolean validateInput() {
    final String errorMessage = getErrorMessage();
    if (errorMessage != null) {
      Messages.showErrorDialog(myProject, errorMessage, TITLE);
      return false;
    }
    return true;
  }

  @Nullable
  private String getErrorMessage() {
    final String fileName = myKeystoreFileNameTextField.getText().trim();
    if (fileName.length() == 0) {
      return "Keystore file name is empty";
    }

    final String dirPath = myKeystoreFileLocationTextWithBrowse.getText().trim();
    if (dirPath.length() == 0) {
      return "Keystore file location is empty";
    }

    final VirtualFile dir = LocalFileSystem.getInstance().findFileByPath(dirPath);
    if (dir == null || !dir.isDirectory()) {
      return FlexBundle.message("folder.does.not.exist", dirPath);
    }

    final String certName = myCertificateNameTextField.getText().trim();
    if (certName.length() == 0) {
      return "Certificate name is empty";
    }

    final char[] password = myPasswordField.getPassword();
    final char[] confirmPassword = myConfirmPasswordField.getPassword();
    if (!Arrays.equals(password, confirmPassword)) {
      return "Password and its confirmation do not match";
    }

    if (password.length == 0) {
      return "Password is empty";
    }

    final String countryCode = isShowingOptionalParameters() ? myCountryCodeTextField.getText().trim() : "";
    if (countryCode.length() > 0 && countryCode.length() != 2) {
      return "Invalid country code. Only two-letter ISO-3166 country codes can be used";
    }

    return null;
  }

  private void createUIComponents() {
    myOptionalParametersHyperlinkLabel = new HoverHyperlinkLabel(SHOW_OPTIONAL_PARAMETERS);
  }

  public CertificateParameters getCertificateParameters() {
    final String keystorePath = FileUtil
      .toSystemDependentName(myKeystoreFileLocationTextWithBrowse.getText().trim() + "/" + myKeystoreFileNameTextField.getText().trim());
    final String certificateName = myCertificateNameTextField.getText().trim();
    final String keyType = (String)myKeyTypeCombo.getSelectedItem();
    final String password = new String(myPasswordField.getPassword());
    final String orgUnit = isShowingOptionalParameters() ? myOrgUnitTextField.getText().trim() : "";
    final String orgName = isShowingOptionalParameters() ? myOrgNameTextField.getText().trim() : "";
    final String countryCode = isShowingOptionalParameters() ? myCountryCodeTextField.getText().trim() : "";

    return new CertificateParameters(keystorePath, certificateName, keyType, password, orgUnit, orgName, countryCode);
  }

  protected String getHelpId() {
    return "reference.flex.create.html.wrapper.create.certificate";
  }
}
