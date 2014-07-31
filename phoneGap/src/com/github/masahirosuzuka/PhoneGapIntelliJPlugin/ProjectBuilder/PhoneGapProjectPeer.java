package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ProjectBuilder;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.PhoneGapUIUtil;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapCommands;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.platform.WebProjectGenerator;
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class PhoneGapProjectPeer implements WebProjectGenerator.GeneratorPeer<PhoneGapProjectTemplateGenerator.PhoneGapProjectSettings> {

  private final List<WebProjectGenerator.SettingsStateListener> myStateListeners = ContainerUtil.createLockFreeCopyOnWriteList();
  private TextFieldWithHistoryWithBrowseButton myExecutablePathField;

  private final ConcurrentMap<String, Boolean> myValidateCache = ContainerUtil.newConcurrentMap();

  PhoneGapProjectPeer() {
  }

  @NotNull
  @Override
  public JComponent getComponent() {
    myExecutablePathField = PhoneGapUIUtil.createPhoneGapExecutableTextField(null);
    JPanel panel = FormBuilder.createFormBuilder()
      .addLabeledComponent(getLabel(), myExecutablePathField)
      .getPanel();

    panel.setPreferredSize(new Dimension(600, 40));
    return panel;
  }

  protected String getLabel() {
    return "Phonegap/Cordova executable:";
  }

  @Override
  public void buildUI(@NotNull SettingsStep settingsStep) {
    myExecutablePathField = PhoneGapUIUtil.createPhoneGapExecutableTextField(null);
    settingsStep.addSettingsField(getLabel(), myExecutablePathField);
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
        if (StringUtil.isEmpty(path)) {
          return new ValidationInfo("Please select path to executable");
        }

        new PhoneGapCommands(path, null).version();
        error = false;
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
}
