package org.intellij.plugins.markdown.settings;

import com.intellij.ide.ui.LafManager;
import com.intellij.ide.ui.LafManagerListener;

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

    final MarkdownCssSettings currentCssSettings = MarkdownApplicationSettings.getInstance().getMarkdownCssSettings();
    if (MarkdownCssSettings.getDefaultCssSettings(isLastLAFWasDarcula).equals(currentCssSettings)) {
      MarkdownApplicationSettings.getInstance().setMarkdownCssSettings(MarkdownCssSettings.getDefaultCssSettings(isNewLookAndFeelDarcula));
    }
    isLastLAFWasDarcula = isNewLookAndFeelDarcula;
  }

  private static boolean isDarcula(UIManager.LookAndFeelInfo laf) {
    return laf.getName().contains("Darcula");
  }
}