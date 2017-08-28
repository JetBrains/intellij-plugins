package org.intellij.plugins.markdown.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.Disposer;
import org.intellij.plugins.markdown.MarkdownBundle;
import org.intellij.plugins.markdown.ui.actions.MarkdownActionUtil;
import org.intellij.plugins.markdown.ui.split.SplitFileEditor;
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
    return MarkdownBundle.message("settings.markdown.name");
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

    SplitFileEditor.SplitEditorLayout oldLayout = myMarkdownApplicationSettings.getMarkdownPreviewSettings().getSplitEditorLayout();

    myMarkdownApplicationSettings.setMarkdownCssSettings(form.getMarkdownCssSettings());
    myMarkdownApplicationSettings.setMarkdownPreviewSettings(form.getMarkdownPreviewSettings());

    SplitFileEditor.SplitEditorLayout newLayout = myMarkdownApplicationSettings.getMarkdownPreviewSettings().getSplitEditorLayout();

    updateOpenedEditorsLayout(oldLayout, newLayout);
  }

  private static void updateOpenedEditorsLayout(@NotNull SplitFileEditor.SplitEditorLayout oldLayout,
                                                @NotNull SplitFileEditor.SplitEditorLayout newLayout) {
    for (Project project : ProjectManager.getInstance().getOpenProjects()) {
      ApplicationManager.getApplication().invokeLater(() -> {
        for (FileEditor fileEditor : FileEditorManagerEx.getInstanceEx(project).getAllEditors()) {
          final SplitFileEditor splitFileEditor = MarkdownActionUtil.findSplitEditor(fileEditor);

          if (splitFileEditor != null && splitFileEditor.getCurrentEditorLayout() == oldLayout) {
            splitFileEditor.triggerLayoutChange(newLayout);
          }
        }
      });
    }
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
