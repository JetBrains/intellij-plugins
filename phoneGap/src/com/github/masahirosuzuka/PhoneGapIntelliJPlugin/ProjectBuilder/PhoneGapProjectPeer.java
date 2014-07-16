package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ProjectBuilder;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.util.PhoneGapSettings;
import com.intellij.execution.util.ExecUtil;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.platform.WebProjectGenerator;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class PhoneGapProjectPeer implements WebProjectGenerator.GeneratorPeer<PhoneGapProjectTemplateGenerator.PhoneGapProjectSettings> {

  private final List<WebProjectGenerator.SettingsStateListener> myStateListeners = ContainerUtil.createLockFreeCopyOnWriteList();
  private TextFieldWithBrowseButton myExecutablePathField;

  private final ConcurrentMap<String, Boolean> myValidateCache = ContainerUtil.newConcurrentMap();

  PhoneGapProjectPeer() {
    myExecutablePathField = new TextFieldWithBrowseButton();
    myExecutablePathField.setText(getExecutable(PhoneGapSettings.getInstance()));
  }

  private void init() {
    myExecutablePathField.addBrowseFolderListener(
      new TextBrowseFolderListener(FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor()));
  }

  @NotNull
  @Override
  public JComponent getComponent() {
    JPanel panel = FormBuilder.createFormBuilder()
      .addLabeledComponent(getLabel(), myExecutablePathField)
      .getPanel();
    init();

    return panel;
  }

  protected String getLabel() {
    return "Phonegap/Cordova executable";
  }

  @Override
  public void buildUI(@NotNull SettingsStep settingsStep) {
    settingsStep.addSettingsField(getLabel(), myExecutablePathField);
    init();
  }

  @NotNull
  @Override
  public PhoneGapProjectTemplateGenerator.PhoneGapProjectSettings getSettings() {
    PhoneGapProjectTemplateGenerator.PhoneGapProjectSettings settings = new PhoneGapProjectTemplateGenerator.PhoneGapProjectSettings();
    settings.setExecutable(myExecutablePathField.getText());
    return settings;
  }

  @Nullable
  @Override
  public ValidationInfo validate() {

    String path = myExecutablePathField.getText();

    boolean error;

    if (myValidateCache.containsKey(path)) {
      error = myValidateCache.get(path);
    }
    else {
      try {
        error = ExecUtil.execAndGetResult(path, "--version") > 0;
      }
      catch (Exception e) {
        error = true;
      }
      myValidateCache.put(path, error);
    }
    return error ? new ValidationInfo("Incorrect Phonegap/Cordova executable") : null;
  }

  @Override
  public boolean isBackgroundJobRunning() {
    return false;
  }

  @Override
  public void addSettingsStateListener(@NotNull WebProjectGenerator.SettingsStateListener listener) {
    myStateListeners.add(listener);
  }

  private static String getExecutable(PhoneGapSettings settings) {
    String executable = "";
    if (settings.isPhoneGapAvailable()) {
      executable = settings.getPhoneGapExecutablePath();
    }
    else if (settings.isCordovaAvailable()) {
      executable = settings.getCordovaExecutablePath();
    }
    return executable;
  }
}
