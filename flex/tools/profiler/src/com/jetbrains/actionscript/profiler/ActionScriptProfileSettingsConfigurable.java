package com.jetbrains.actionscript.profiler;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.jetbrains.actionscript.profiler.model.ActionScriptProfileSettings;
import com.jetbrains.actionscript.profiler.ui.ActionScriptProfileSettingsForm;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author: Fedor.Korotkov
 */
public class ActionScriptProfileSettingsConfigurable implements SearchableConfigurable {
  private final ActionScriptProfileSettings mySettings = ActionScriptProfileSettings.getInstance();
  private ActionScriptProfileSettingsForm mySettingsPane;

  public String getDisplayName() {
    return ProfilerBundle.message("profile.settings.name");
  }

  @NotNull
  public String getId() {
    return "asprofile.settings";
  }

  public String getHelpTopic() {
    return null;
  }

  public JComponent createComponent() {
    if (mySettingsPane == null) {
      mySettingsPane = new ActionScriptProfileSettingsForm();
    }
    reset();
    return mySettingsPane.getPanel();
  }

  public boolean isModified() {
    return mySettingsPane != null && mySettingsPane.isModified(mySettings);
  }

  public void apply() throws ConfigurationException {
    if (mySettingsPane != null) {
      mySettingsPane.applyEditorTo(mySettings);
    }
  }

  public void reset() {
    if (mySettingsPane != null) {
      mySettingsPane.resetEditorFrom(mySettings);
    }
  }

  public void disposeUIResources() {
    mySettingsPane = null;
  }

  public Runnable enableSearch(String option) {
    return null;
  }
}
