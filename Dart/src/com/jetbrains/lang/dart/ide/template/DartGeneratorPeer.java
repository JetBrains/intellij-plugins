package com.jetbrains.lang.dart.ide.template;

import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.WebProjectGenerator;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.DartSdkData;
import com.jetbrains.lang.dart.ide.settings.DartSettings;
import com.jetbrains.lang.dart.ide.settings.DartSettingsUtil;
import com.jetbrains.lang.dart.util.DartSdkUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DartGeneratorPeer implements WebProjectGenerator.GeneratorPeer<DartSdkData> {
  private JPanel myMainPanel;
  private TextFieldWithBrowseButton mySdkPath;

  public DartGeneratorPeer() {
    mySdkPath.getButton().addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
        final VirtualFile file = FileChooser.chooseFile(descriptor, mySdkPath, null, null);
        if (file != null) {
          mySdkPath.setText(FileUtil.toSystemDependentName(file.getPath()));
        }
      }
    });

    DartSettings dartSettings = DartSettingsUtil.getSettings();
    mySdkPath.setText(FileUtil.toSystemDependentName(dartSettings.getSdkPath()));
  }

  @NotNull
  @Override
  public JComponent getComponent() {
    return myMainPanel;
  }

  @Override
  public void buildUI(@NotNull SettingsStep settingsStep) {
    settingsStep.addSettingsField(DartBundle.message("dart.choose.sdk.home"), mySdkPath);
  }

  @NotNull
  @Override
  public DartSdkData getSettings() {
    DartSdkData sdkData = getSdkData();
    return sdkData != null ? sdkData : new DartSdkData(mySdkPath.getText(), "NA");
  }

  @Nullable
  @Override
  public ValidationInfo validate() {
    if (getSdkData() == null) {
      return new ValidationInfo(DartBundle.message("dart.sdk.bad.path", mySdkPath.getText()));
    }
    return null;
  }

  @Nullable
  private DartSdkData getSdkData() {
    return DartSdkUtil.testDartSdk(FileUtil.toSystemIndependentName(mySdkPath.getText()));
  }

  @Override
  public boolean isBackgroundJobRunning() {
    return false;
  }

  @Override
  public void addSettingsStateListener(@NotNull WebProjectGenerator.SettingsStateListener listener) {
  }
}
