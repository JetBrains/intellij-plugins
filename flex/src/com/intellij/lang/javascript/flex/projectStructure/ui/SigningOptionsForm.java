// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
import com.intellij.ui.components.JBRadioButton;
import com.intellij.util.ui.PlatformColors;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

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

  private JLabel myTargetArchitectureLabel;
  private JBRadioButton myArchArmv7RadioButton;
  private JBRadioButton myArchX86RadioButton;
  private JBRadioButton myArchArmv8RadioButton;

  private JCheckBox myUseTempCertificateCheckBox;

  private JLabel myProvisioningProfileLabel;
  private TextFieldWithBrowseButton myProvisioningProfileTextWithBrowse;
  private JLabel myKeystoreFileLabel;
  private TextFieldWithBrowseButton myKeystoreFileTextWithBrowse;
  private JLabel myIosSdkLabel;
  private TextFieldWithBrowseButton myIosSdkTextWithBrowse;

  private HoverHyperlinkLabel myMoreOptionsHyperlinkLabel;

  private JLabel myKeystoreTypeLabel;
  private JTextField myKeystoreTypeTextField;
  private JLabel myKeyAliasLabel;
  private JTextField myKeyAliasTextField;
  private JLabel myProviderClassNameLabel;
  private JTextField myProviderClassNameTextField;
  private JLabel myTsaUrlLabel;
  private JTextField myTsaUrlTextField;

  private JLabel myAdtOptionsLabel;
  private RawCommandLineEditor myAdtOptionsComponent;

  enum Mode {Desktop, Android, iOS}

  public SigningOptionsForm(final Project project, final Mode mode) {
    myMode = mode;
    myUseTempCertificateCheckBox.addActionListener(new ActionListener() {
      @Override
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
      @Override
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          showMoreOptions(!isShowingMoreOptions());
        }
      }
    });

    updateMoreOptions();

    switch (mode) {
      case Desktop:
        myTargetArchitectureLabel.setVisible(false);
        myArchArmv7RadioButton.setVisible(false);
        myArchX86RadioButton.setVisible(false);
        myArchArmv8RadioButton.setVisible(false);
        // no break here
      case Android:
        myProvisioningProfileLabel.setVisible(false);
        myProvisioningProfileTextWithBrowse.setVisible(false);
        myIosSdkLabel.setVisible(false);
        myIosSdkTextWithBrowse.setVisible(false);
        myAdtOptionsLabel.setVisible(false);
        myAdtOptionsComponent.setVisible(false);
        break;
      case iOS:
        myTargetArchitectureLabel.setVisible(false);
        myArchArmv7RadioButton.setVisible(false);
        myArchX86RadioButton.setVisible(false);
        myArchArmv8RadioButton.setVisible(false);
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
    final boolean enabled = !myUseTempCertificateCheckBox.isVisible() || !myUseTempCertificateCheckBox.isSelected();

    myProvisioningProfileLabel.setEnabled(enabled);
    myProviderClassNameTextField.setEnabled(enabled);
    myKeystoreFileLabel.setEnabled(enabled);
    myKeystoreFileTextWithBrowse.setEnabled(enabled);
    myIosSdkLabel.setEnabled(enabled);
    myIosSdkTextWithBrowse.setEnabled(enabled);

    myMoreOptionsHyperlinkLabel.setEnabled(enabled);
    if (myMoreOptionsHyperlinkLabel.isEnabled()) {
      myMoreOptionsHyperlinkLabel.setForeground(PlatformColors.BLUE); // workaround of JLabel-related workaround at UIUtil.setEnabled(..)
    }

    myKeystoreTypeLabel.setEnabled(enabled);
    myKeystoreTypeTextField.setEnabled(enabled);
    myKeyAliasLabel.setEnabled(enabled);
    myKeyAliasTextField.setEnabled(enabled);
    myProviderClassNameLabel.setEnabled(enabled);
    myProviderClassNameTextField.setEnabled(enabled);
    myTsaUrlLabel.setEnabled(enabled);
    myTsaUrlTextField.setEnabled(enabled);
  }

  public void setEnabled(final boolean enabled) {
    UIUtil.setEnabled(myMainPanel, enabled, true);
    if (enabled) {
      updateControls();
    }
  }

  @NotNull
  private String getArch() {
    if (myArchX86RadioButton.isSelected()) return AirSigningOptions.ARCH_X86;
    if (myArchArmv8RadioButton.isSelected()) return AirSigningOptions.ARCH_ARMV8;
    return AirSigningOptions.ARCH_ARMV7;
  }

  public void resetFrom(final AirSigningOptions signingOptions) {
    if (signingOptions.getArch().equals(AirSigningOptions.ARCH_X86)) {
      myArchX86RadioButton.setSelected(true);
    }
    else if (signingOptions.getArch().equals(AirSigningOptions.ARCH_ARMV8)) {
      myArchArmv8RadioButton.setSelected(true);
    }
    else {
      myArchArmv7RadioButton.setSelected(true);
    }
    myUseTempCertificateCheckBox.setSelected(signingOptions.isUseTempCertificate());
    myProvisioningProfileTextWithBrowse.setText(FileUtil.toSystemDependentName(signingOptions.getProvisioningProfilePath()));
    myKeystoreFileTextWithBrowse.setText(FileUtil.toSystemDependentName(signingOptions.getKeystorePath()));
    myIosSdkTextWithBrowse.setText(FileUtil.toSystemDependentName(signingOptions.getIOSSdkPath()));
    myAdtOptionsComponent.setText(signingOptions.getADTOptions());
    myKeystoreTypeTextField.setText(signingOptions.getKeystoreType());
    myKeyAliasTextField.setText(signingOptions.getKeyAlias());
    myProviderClassNameTextField.setText(signingOptions.getProvider());
    myTsaUrlTextField.setText(signingOptions.getTsa());
  }

  public boolean isModified(final AirSigningOptions signingOptions) {
    if (myTargetArchitectureLabel.isVisible() && !getArch().equals(signingOptions.getArch())) return true;

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
    signingOptions.setArch(getArch());
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
    return switch (location) {
      case ProvisioningProfile ->
        IdeFocusManager.findInstance().requestFocus(myProvisioningProfileTextWithBrowse.getChildComponent(), true);
      case Keystore -> IdeFocusManager.findInstance().requestFocus(myKeystoreFileTextWithBrowse.getChildComponent(), true);
      case IosSdkPath -> IdeFocusManager.findInstance().requestFocus(myIosSdkTextWithBrowse.getTextField(), true);
      default -> ActionCallback.DONE;
    };
  }
}
