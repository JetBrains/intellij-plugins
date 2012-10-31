package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.projectStructure.model.AirSigningOptions;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.HoverHyperlinkLabel;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.util.ui.PlatformColors;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SigningOptionsForm {
  private static final String MORE_OPTIONS = "More options";
  private static final String LESS_OPTIONS = "Less options";

  private final Mode myMode;

  private JPanel myMainPanel;

  private JCheckBox myUseTempCertificateCheckBox;
  private JPanel myOptionsPanel;

  private JLabel myProvisioningProfileLabel;
  private TextFieldWithBrowseButton myProvisioningProfileTextWithBrowse;
  private TextFieldWithBrowseButton myKeystoreFileTextWithBrowse;
  private JLabel myIosSdkLabel;
  private TextFieldWithBrowseButton myIosSdkTextWithBrowse;
  private JLabel myAdtOptionsLabel;
  private RawCommandLineEditor myAdtOptionsComponent;
  private HoverHyperlinkLabel myMoreOptionsHyperlinkLabel;
  private JLabel myKeystoreTypeLabel;
  private JTextField myKeystoreTypeTextField;
  private JLabel myKeyAliasLabel;
  private JTextField myKeyAliasTextField;
  private JLabel myProviderClassNameLabel;
  private JTextField myProviderClassNameTextField;
  private JLabel myTsaUrlLabel;
  private JTextField myTsaUrlTextField;

  enum Mode {Desktop, Android, iOS}

  public SigningOptionsForm(final Project project, final Mode mode) {
    myMode = mode;
    myUseTempCertificateCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        updateControls();
      }
    });

    myProvisioningProfileTextWithBrowse
      .addBrowseFolderListener(null, null, project, FlexUtils.createFileChooserDescriptor("mobileprovision"));

    myIosSdkTextWithBrowse
      .addBrowseFolderListener(null, null, project, FileChooserDescriptorFactory.createSingleFolderDescriptor());

    final FileChooserDescriptor d = mode == Mode.iOS ? FlexUtils.createFileChooserDescriptor("p12")
                                                     : FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor();
    myKeystoreFileTextWithBrowse.addBrowseFolderListener(null, null, project, d);

    myAdtOptionsComponent.setDialogCaption("Additional ADT Options");

    myMoreOptionsHyperlinkLabel.setText(MORE_OPTIONS);
    myMoreOptionsHyperlinkLabel.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          showMoreOptions(!isShowingMoreOptions());
        }
      }
    });

    updateMoreOptions();

    switch (mode) {
      case Desktop:
      case Android:
        myProvisioningProfileLabel.setVisible(false);
        myProvisioningProfileTextWithBrowse.setVisible(false);
        myIosSdkLabel.setVisible(false);
        myIosSdkTextWithBrowse.setVisible(false);
        myAdtOptionsLabel.setVisible(false);
        myAdtOptionsComponent.setVisible(false);
        break;
      case iOS:
        myUseTempCertificateCheckBox.setVisible(false);
        showMoreOptions(false);
        myMoreOptionsHyperlinkLabel.setVisible(false);
        break;
    }
  }

  private void createUIComponents() {
    myMoreOptionsHyperlinkLabel = new HoverHyperlinkLabel(MORE_OPTIONS);
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

    myKeystoreTypeLabel.setVisible(showingMoreOption);
    myKeystoreTypeTextField.setVisible(showingMoreOption);
    myKeyAliasLabel.setVisible(showingMoreOption);
    myKeyAliasTextField.setVisible(showingMoreOption);
    myProviderClassNameLabel.setVisible(showingMoreOption);
    myProviderClassNameTextField.setVisible(showingMoreOption);
    myTsaUrlLabel.setVisible(showingMoreOption && myMode != Mode.Android);
    myTsaUrlTextField.setVisible(showingMoreOption && myMode != Mode.Android);
  }

  private void updateControls() {
    UIUtil.setEnabled(myOptionsPanel, !myUseTempCertificateCheckBox.isVisible() || !myUseTempCertificateCheckBox.isSelected(), true);
    if (myMoreOptionsHyperlinkLabel.isEnabled()) {
      myMoreOptionsHyperlinkLabel.setForeground(PlatformColors.BLUE); // workaround of JLabel-related workaround at UIUtil.setEnabled(..)
    }
  }

  public void setEnabled(final boolean enabled) {
    UIUtil.setEnabled(myMainPanel, enabled, true);
    if (enabled) {
      updateControls();
    }
  }

  public void setTempCertificateApplicable(final boolean applicable) {
    myUseTempCertificateCheckBox.setVisible(applicable);
    updateControls();
  }

  public void setProvisioningProfileApplicable(final boolean applicable) {
    myProvisioningProfileLabel.setVisible(applicable);
    myProvisioningProfileTextWithBrowse.setVisible(applicable);
  }

  public void resetFrom(final AirSigningOptions signingOptions) {
    myUseTempCertificateCheckBox.setSelected(signingOptions.isUseTempCertificate());
    myProvisioningProfileTextWithBrowse.setText(FileUtil.toSystemDependentName(signingOptions.getProvisioningProfilePath()));
    myKeystoreFileTextWithBrowse.setText(FileUtil.toSystemDependentName(signingOptions.getKeystorePath()));
    myIosSdkTextWithBrowse.setText(FileUtil.toSystemDependentName(signingOptions.getIOSSdkPath()));
    myAdtOptionsComponent.setText(signingOptions.getADTOptions());
    myKeystoreTypeTextField.setText(signingOptions.getKeystoreType());
    myKeyAliasTextField.setText(signingOptions.getKeyAlias());
    myProviderClassNameTextField.setText(signingOptions.getProvider());
    myTsaUrlTextField.setText(signingOptions.getTsa());
    updateControls();
  }

  public boolean isModified(final AirSigningOptions signingOptions) {
    if (myUseTempCertificateCheckBox.isVisible() && myUseTempCertificateCheckBox.isSelected() != signingOptions.isUseTempCertificate()) {
      return true;
    }

    final String profilePath = FileUtil.toSystemIndependentName(myProvisioningProfileTextWithBrowse.getText().trim());
    if (myProvisioningProfileTextWithBrowse.isVisible() && !profilePath.equals(signingOptions.getProvisioningProfilePath())) return true;

    final String keystorePath = FileUtil.toSystemIndependentName(myKeystoreFileTextWithBrowse.getText().trim());
    if (!keystorePath.equals(signingOptions.getKeystorePath())) return true;

    final String iosSdkPath = FileUtil.toSystemIndependentName(myIosSdkTextWithBrowse.getText().trim());
    if (myIosSdkTextWithBrowse.isVisible() && !iosSdkPath.equals(signingOptions.getIOSSdkPath())) return true;

    if (myAdtOptionsComponent.isVisible() && !myAdtOptionsComponent.getText().equals(signingOptions.getADTOptions())) return false;
    if (!myKeystoreTypeTextField.getText().trim().equals(signingOptions.getKeystoreType())) return true;
    if (!myKeyAliasTextField.getText().equals(signingOptions.getKeyAlias())) return true;
    if (!myProviderClassNameTextField.getText().equals(signingOptions.getProvider())) return true;
    if (!myTsaUrlTextField.getText().equals(signingOptions.getTsa())) return true;

    return false;
  }

  public void applyTo(final AirSigningOptions signingOptions) {
    signingOptions.setUseTempCertificate(myUseTempCertificateCheckBox.isSelected());
    signingOptions.setProvisioningProfilePath(FileUtil.toSystemIndependentName(myProvisioningProfileTextWithBrowse.getText().trim()));
    signingOptions.setKeystorePath(FileUtil.toSystemIndependentName(myKeystoreFileTextWithBrowse.getText().trim()));
    signingOptions.setIOSSdkPath(FileUtil.toSystemIndependentName(myIosSdkTextWithBrowse.getText().trim()));
    signingOptions.setADTOptions(myAdtOptionsComponent.getText().trim());
    signingOptions.setKeystoreType(myKeystoreTypeTextField.getText().trim());
    signingOptions.setKeyAlias(myKeyAliasTextField.getText());
    signingOptions.setProvider(myProviderClassNameTextField.getText());
    signingOptions.setTsa(myTsaUrlTextField.getText());
  }

  public ActionCallback navigateTo(final AirPackagingConfigurableBase.Location location) {
    switch (location) {
      case ProvisioningProfile:
        return IdeFocusManager.findInstance().requestFocus(myProvisioningProfileTextWithBrowse.getChildComponent(), true);
      case Keystore:
        return IdeFocusManager.findInstance().requestFocus(myKeystoreFileTextWithBrowse.getChildComponent(), true);
      case IosSdkPath:
        return IdeFocusManager.findInstance().requestFocus(myIosSdkTextWithBrowse.getTextField(), true);
      default:
        return new ActionCallback.Done();
    }
  }
}
