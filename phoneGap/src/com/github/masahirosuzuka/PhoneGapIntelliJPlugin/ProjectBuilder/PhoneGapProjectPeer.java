package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ProjectBuilder;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.PhoneGapBundle;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.PhoneGapUtil;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapCommandLine;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.platform.WebProjectGenerator;
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
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
    setFields();
    JPanel panel = FormBuilder.createFormBuilder()
      .addLabeledComponent(PhoneGapBundle.message("phonegap.project.builder.label"), myExecutablePathField)
      .getPanel();

    panel.setPreferredSize(JBUI.size(600, 40));
    return panel;
  }

  @Override
  public void buildUI(@NotNull SettingsStep settingsStep) {
    setFields();
    settingsStep.addSettingsField(PhoneGapBundle.message("phonegap.project.builder.label"), myExecutablePathField);
  }

  @NotNull
  @Override
  public PhoneGapProjectTemplateGenerator.PhoneGapProjectSettings getSettings() {
    PhoneGapProjectTemplateGenerator.PhoneGapProjectSettings settings = new PhoneGapProjectTemplateGenerator.PhoneGapProjectSettings();
    settings.setExecutable(myExecutablePathField.getText());

    return settings;
  }

  private void setFields() {
    myExecutablePathField = PhoneGapUtil.createPhoneGapExecutableTextField(null);
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
          return new ValidationInfo(PhoneGapBundle.message("phonegap.incorrect.path.executable"));
        }

        new PhoneGapCommandLine(path, null).version();
        error = false;
      }
      catch (Exception e) {
        error = true;
      }
      myValidateCache.put(path, error);
    }
    return error ? new ValidationInfo(PhoneGapBundle.message("phonegap.incorrect.path.error")) : null;
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
