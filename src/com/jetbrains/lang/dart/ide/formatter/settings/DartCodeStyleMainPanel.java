package com.jetbrains.lang.dart.ide.formatter.settings;

import com.intellij.application.options.TabbedLanguageCodeStylePanel;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.jetbrains.lang.dart.DartLanguage;

/**
 * @author: Fedor.Korotkov
 */
public class DartCodeStyleMainPanel extends TabbedLanguageCodeStylePanel {
  protected DartCodeStyleMainPanel(CodeStyleSettings currentSettings, CodeStyleSettings settings) {
    super(DartLanguage.INSTANCE, currentSettings, settings);
  }
}
