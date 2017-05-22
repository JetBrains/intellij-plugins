package com.jetbrains.lang.dart.ide.formatter.settings;

import com.intellij.application.options.TabbedLanguageCodeStylePanel;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.ide.application.options.DartCodeGenerationPanel;
import org.jetbrains.annotations.NotNull;

public class DartCodeStyleMainPanel extends TabbedLanguageCodeStylePanel {
  protected DartCodeStyleMainPanel(CodeStyleSettings currentSettings, CodeStyleSettings settings) {
    super(DartLanguage.INSTANCE, currentSettings, settings);
  }

  @Override
  protected void initTabs(@NotNull final CodeStyleSettings settings) {
    super.initTabs(settings);
    addTab(new DartCodeGenerationPanel(settings));
  }
}
