package org.intellij.plugins.markdown.settings;

import com.intellij.ide.ui.LafManager;
import com.intellij.ide.ui.LafManagerListener;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

class MarkdownLAFListener implements LafManagerListener {
  private boolean isLastLAFWasDarcula = isDarcula(LafManager.getInstance().getCurrentLookAndFeel());

  @Override
  public void lookAndFeelChanged(LafManager source) {
    final UIManager.LookAndFeelInfo newLookAndFeel = source.getCurrentLookAndFeel();
    final boolean isNewLookAndFeelDarcula = isDarcula(newLookAndFeel);

    if (isNewLookAndFeelDarcula == isLastLAFWasDarcula) {
      return;
    }

    updateCssSettingsForced(isNewLookAndFeelDarcula);
  }

  public void updateCssSettingsForced(boolean isDarcula) {
    final MarkdownCssSettings currentCssSettings = MarkdownApplicationSettings.getInstance().getMarkdownCssSettings();
    MarkdownApplicationSettings.getInstance().setMarkdownCssSettings(new MarkdownCssSettings(
      currentCssSettings.isUriEnabled(),
      MarkdownCssSettings.getDefaultCssSettings(isDarcula).getStylesheetUri(),
      currentCssSettings.isTextEnabled(),
      currentCssSettings.getStylesheetText()
    ));
    isLastLAFWasDarcula = isDarcula;
  }

  public static boolean isDarcula(@Nullable UIManager.LookAndFeelInfo laf) {
    if (laf == null) {
      return false;
    }
    return laf.getName().contains("Darcula");
  }
}