package org.intellij.plugins.markdown.settings;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.util.Disposer;
import org.intellij.plugins.markdown.MarkdownBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class MarkdownSettingsConfigurable implements SearchableConfigurable {
  @Nullable
  private MarkdownSettingsForm myForm = null;
  @NotNull
  private MarkdownApplicationSettings myMarkdownApplicationSettings;

  public MarkdownSettingsConfigurable(@NotNull MarkdownApplicationSettings markdownApplicationSettings) {
    myMarkdownApplicationSettings = markdownApplicationSettings;
  }

  @NotNull
  @Override
  public String getId() {
    return "Settings.Markdown";
  }

  @Nls
  @Override
  public String getDisplayName() {
    return MarkdownBundle.message("markdown.settings.name");
  }

  @NotNull
  @Override
  public JComponent createComponent() {
    return getForm().getComponent();
  }

  @NotNull
  public MarkdownSettingsForm getForm() {
    if (myForm == null) {
      myForm = new MarkdownSettingsForm();
    }
    return myForm;
  }

  @Override
  public boolean isModified() {
    return !getForm().getMarkdownCssSettings().equals(myMarkdownApplicationSettings.getMarkdownCssSettings()) ||
           !getForm().getMarkdownPreviewSettings().equals(myMarkdownApplicationSettings.getMarkdownPreviewSettings());
  }

  @Override
  public void apply() throws ConfigurationException {
    final MarkdownSettingsForm form = getForm();
    form.validate();

    myMarkdownApplicationSettings.setMarkdownCssSettings(form.getMarkdownCssSettings());
    myMarkdownApplicationSettings.setMarkdownPreviewSettings(form.getMarkdownPreviewSettings());
  }

  @Override
  public void reset() {
    getForm().setMarkdownCssSettings(myMarkdownApplicationSettings.getMarkdownCssSettings());
    getForm().setMarkdownPreviewSettings(myMarkdownApplicationSettings.getMarkdownPreviewSettings());
  }

  @Override
  public void disposeUIResources() {
    if (myForm != null) {
      Disposer.dispose(myForm);
    }
    myForm = null;
  }
}
