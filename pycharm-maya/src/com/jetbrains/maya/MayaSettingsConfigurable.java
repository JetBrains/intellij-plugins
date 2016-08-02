package com.jetbrains.maya;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author traff
 */
public class MayaSettingsConfigurable implements SearchableConfigurable, Configurable.NoScroll, Disposable {
  public static final String CONSOLE_SETTINGS_HELP_REFERENCE = "reference.settings.ssh.terminal";

  private MayaSettingsPanel myPanel;

  private final MayaSettingsProvider mySettingsProvider;
  private Project myProject;

  public MayaSettingsConfigurable(Project project) {
    mySettingsProvider = MayaSettingsProvider.getInstance(project);
    myProject = project;
  }

  @NotNull
  @Override
  public String getId() {
    return "maya";
  }

  @Nls
  @Override
  public String getDisplayName() {
    return "Maya";
  }

  @Override
  public String getHelpTopic() {
    return CONSOLE_SETTINGS_HELP_REFERENCE;
  }

  @Override
  public JComponent createComponent() {
    myPanel = new MayaSettingsPanel(mySettingsProvider);

    return myPanel.createPanel();
  }

  @Override
  public boolean isModified() {
    return myPanel.isModified();
  }

  @Override
  public void apply() throws ConfigurationException {
    myPanel.apply();
  }


  @Override
  public void reset() {
    myPanel.reset();
  }

  @Override
  public void disposeUIResources() {
    Disposer.dispose(this);
  }

  @Override
  public void dispose() {
    myPanel = null;
  }
}
