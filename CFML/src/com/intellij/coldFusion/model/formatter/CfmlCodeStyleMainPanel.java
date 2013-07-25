package com.intellij.coldFusion.model.formatter;

import com.intellij.application.options.TabbedLanguageCodeStylePanel;
import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.psi.codeStyle.CodeStyleSettings;

/**
 * Created by IntelliJ IDEA.
 * User: Nadya.Zabrodina
 */
public class CfmlCodeStyleMainPanel extends TabbedLanguageCodeStylePanel {

  protected CfmlCodeStyleMainPanel(CodeStyleSettings currentSettings, CodeStyleSettings settings) {
    super(CfmlLanguage.INSTANCE, currentSettings, settings);
  }
}
