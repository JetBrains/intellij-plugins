package com.jetbrains.lang.dart.ide.formatter.settings;

import com.intellij.application.options.TabbedLanguageCodeStylePanel;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider;
import com.jetbrains.lang.dart.DartLanguage;

public class DartCodeStyleMainPanel extends TabbedLanguageCodeStylePanel {
  protected DartCodeStyleMainPanel(CodeStyleSettings currentSettings, CodeStyleSettings settings) {
    super(DartLanguage.INSTANCE, currentSettings, settings);
  }

  @Override
  protected void initTabs(CodeStyleSettings settings) {
    super.initTabs(settings);
    for (CodeStyleSettingsProvider provider : Extensions.getExtensions(CodeStyleSettingsProvider.EXTENSION_POINT_NAME)) {
      if (provider.getLanguage() == DartLanguage.INSTANCE && !provider.hasSettingsPage()) {
        createTab(provider);
      }
    }
  }
}
