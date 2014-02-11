package com.jetbrains.lang.dart.ide.template;

import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.platform.WebProjectGenerator;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBLabel;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;

public class DartGeneratorPeer implements WebProjectGenerator.GeneratorPeer<String> {
  private JPanel myMainPanel;
  private TextFieldWithBrowseButton mySdkPathTextWithBrowse;
  private JBLabel myVersionLabel;

  public DartGeneratorPeer() {
    DartSdkUtil.initDartSdkPathTextFieldWithBrowseButton(null, mySdkPathTextWithBrowse, myVersionLabel);

    final DartSdk sdk = DartSdk.getGlobalDartSdk();
    mySdkPathTextWithBrowse.setText(sdk == null ? "" : FileUtil.toSystemDependentName(sdk.getHomePath()));
  }

  @NotNull
  @Override
  public JComponent getComponent() {
    return myMainPanel;
  }

  @Override
  public void buildUI(final @NotNull SettingsStep settingsStep) {
    settingsStep.addSettingsField(DartBundle.message("dart.sdk.path.label"), mySdkPathTextWithBrowse);
    settingsStep.addSettingsField(DartBundle.message("version.label"), myVersionLabel);
  }

  @NotNull
  @Override
  public String getSettings() {
    return FileUtil.toSystemIndependentName(mySdkPathTextWithBrowse.getText().trim());
  }

  @Nullable
  @Override
  public ValidationInfo validate() {
    final String message = DartSdkUtil.getErrorMessageIfWrongSdkRootPath(mySdkPathTextWithBrowse.getText().trim());
    return message == null ? null : new ValidationInfo(message, mySdkPathTextWithBrowse);
  }

  @Override
  public boolean isBackgroundJobRunning() {
    return false;
  }

  @Override
  public void addSettingsStateListener(final @NotNull WebProjectGenerator.SettingsStateListener stateListener) {
    mySdkPathTextWithBrowse.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
      protected void textChanged(final DocumentEvent e) {
        stateListener.stateChanged(validate() == null);
      }
    });
  }
}
