package com.jetbrains.plugins.jade.formatter;

import com.intellij.application.options.TabbedLanguageCodeStylePanel;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.jetbrains.plugins.jade.JadeLanguage;

public class JadeCodeStyleMainPanel extends TabbedLanguageCodeStylePanel {

  protected JadeCodeStyleMainPanel(CodeStyleSettings currentSettings, CodeStyleSettings settings) {
    super(JadeLanguage.INSTANCE, currentSettings, settings);
  }

  @Override
  protected void initTabs(CodeStyleSettings settings) {
    addIndentOptionsTab(settings);
  }

}
