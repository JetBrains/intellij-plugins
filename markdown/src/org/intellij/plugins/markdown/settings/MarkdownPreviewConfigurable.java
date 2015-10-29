package org.intellij.plugins.markdown.settings;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import org.intellij.plugins.markdown.MarkdownBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class MarkdownPreviewConfigurable implements SearchableConfigurable {
  @Nullable
  private MarkdownPreviewSettingsForm myForm = null;
  @NotNull
  private MarkdownApplicationSettings myMarkdownApplicationSettings;

  public MarkdownPreviewConfigurable(@NotNull MarkdownApplicationSettings markdownApplicationSettings) {
    myMarkdownApplicationSettings = markdownApplicationSettings;
  }

  @NotNull
  @Override
  public String getId() {
    return "Settings.Markdown.Preview";
  }

  @Nullable
  @Override
  public Runnable enableSearch(String option) {
    return null;
  }

  @Nls
  @Override
  public String getDisplayName() {
    return MarkdownBundle.message("settings.markdown.preview.name");
  }

  @Nullable
  @Override
  public String getHelpTopic() {
    return null;
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    return getForm().getComponent();
  }

  @Override
  public boolean isModified() {
    return !getForm().getMarkdownPreviewSettings().equals(myMarkdownApplicationSettings.getMarkdownPreviewSettings());
  }

  @Override
  public void apply() throws ConfigurationException {
    myMarkdownApplicationSettings.setMarkdownPreviewSettings(getForm().getMarkdownPreviewSettings());
  }

  @Override
  public void reset() {
    getForm().setMarkdownPreviewSettings(myMarkdownApplicationSettings.getMarkdownPreviewSettings());
  }

  @Override
  public void disposeUIResources() {
    myForm = null;
  }

  @NotNull
  public MarkdownPreviewSettingsForm getForm() {
    if (myForm == null) {
      myForm = new MarkdownPreviewSettingsForm();
    }
    return myForm;
  }
}
