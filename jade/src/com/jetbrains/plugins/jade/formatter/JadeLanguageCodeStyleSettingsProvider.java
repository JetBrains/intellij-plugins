package com.jetbrains.plugins.jade.formatter;

import com.intellij.application.options.IndentOptionsEditor;
import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;
import com.jetbrains.plugins.jade.JadeLanguage;
import com.jetbrains.plugins.jade.highlighter.JadeColorsPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class JadeLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {
  @Override
  public @NotNull Language getLanguage() {
    return JadeLanguage.INSTANCE;
  }

  @Override
  public String getCodeSample(@NotNull SettingsType settingsType) {
    return JadeColorsPage.DEMO_TEXT;
  }

  @Override
  public @Nullable IndentOptionsEditor getIndentOptionsEditor() {
    return new IndentOptionsEditor();
  }

}
