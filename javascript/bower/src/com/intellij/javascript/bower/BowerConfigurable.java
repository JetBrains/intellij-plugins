package com.intellij.javascript.bower;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class BowerConfigurable implements Configurable, Configurable.NoScroll {

  private final Project myProject;
  private BowerView myView;

  public BowerConfigurable(@NotNull Project project) {
    myProject = project;
  }

  @Override
  public @Nls String getDisplayName() {
    return BowerBundle.message("settings.javascript.bower.configurable.name");
  }

  @Override
  public @Nullable String getHelpTopic() {
    return "Settings.JavaScript.Bower";
  }

  @Override
  public @Nullable JComponent createComponent() {
    BowerView view = getView();
    return view.getComponent();
  }

  @Override
  public boolean isModified() {
    BowerView view = getView();
    BowerSettings viewSettings = view.getSettings();
    BowerSettings storedSettings = BowerSettingsManager.getInstance(myProject).getSettings();
    return !viewSettings.equals(storedSettings);
  }

  @Override
  public void apply() throws ConfigurationException {
    BowerView view = getView();
    BowerSettings settings = view.getSettings();
    BowerSettingsManager.getInstance(myProject).setSettings(settings);
  }

  @Override
  public void reset() {
    BowerSettings settings = BowerSettingsManager.getInstance(myProject).getSettings();
    BowerView view = getView();
    view.setSettings(settings);
  }

  private BowerView getView() {
    if (myView == null) {
      myView = new BowerView(myProject);
    }
    return myView;
  }

}
