package org.intellij.plugins.markdown.settings;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.util.Disposer;
import org.intellij.plugins.markdown.MarkdownBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class MarkdownCssConfigurable implements SearchableConfigurable {
  @Nullable
  private MarkdownCssSettingsForm myForm = null;
  @NotNull
  private MarkdownApplicationSettings myMarkdownApplicationSettings;

  public MarkdownCssConfigurable(@NotNull MarkdownApplicationSettings markdownApplicationSettings) {
    myMarkdownApplicationSettings = markdownApplicationSettings;
  }

  @NotNull
  @Override
  public String getId() {
    return "Settings.Markdown.Css";
  }

  @Nullable
  @Override
  public Runnable enableSearch(String option) {
    return null;
  }

  @Nls
  @Override
  public String getDisplayName() {
    return MarkdownBundle.message("settings.markdown.css.name");
  }

  @Nullable
  @Override
  public String getHelpTopic() {
    return null;
  }

  @NotNull
  @Override
  public JComponent createComponent() {
    return getForm().getComponent();
  }

  @NotNull
  public MarkdownCssSettingsForm getForm() {
    if (myForm == null) {
      myForm = new MarkdownCssSettingsForm();
    }
    return myForm;
  }

  @Override
  public boolean isModified() {
    return !getForm().getMarkdownCssSettings().equals(myMarkdownApplicationSettings.getMarkdownCssSettings());
  }

  @Override
  public void apply() throws ConfigurationException {
    myMarkdownApplicationSettings.setMarkdownCssSettings(getForm().getMarkdownCssSettings());
  }

  @Override
  public void reset() {
    getForm().setMarkdownCssSettings(myMarkdownApplicationSettings.getMarkdownCssSettings());
  }

  @Override
  public void disposeUIResources() {
    if (myForm != null) {
      Disposer.dispose(myForm);
    }
    myForm = null;
  }
}
