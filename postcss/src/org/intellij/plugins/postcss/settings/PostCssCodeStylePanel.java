package org.intellij.plugins.postcss.settings;

import com.intellij.application.options.TabbedLanguageCodeStylePanel;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import org.intellij.plugins.postcss.PostCssLanguage;

public class PostCssCodeStylePanel extends TabbedLanguageCodeStylePanel {
  protected PostCssCodeStylePanel(CodeStyleSettings currentSettings, CodeStyleSettings settings) {
    super(PostCssLanguage.INSTANCE, currentSettings, settings);
  }

  @Override
  protected void initTabs(CodeStyleSettings settings) {
    addIndentOptionsTab(settings);
  }
}