package org.intellij.plugins.postcss.settings;

import com.intellij.application.options.CodeStyleAbstractConfigurable;
import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.CodeStyleConfigurable;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;
import org.intellij.plugins.postcss.PostCssLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PostCssStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {
  @Override
  public @NotNull CustomCodeStyleSettings createCustomSettings(@NotNull CodeStyleSettings settings) {
    return new PostCssCodeStyleSettings(settings);
  }

  @Override
  public @Nullable String getCodeSample(@NotNull SettingsType settingsType) {
    return null;
  }

  @Override
  public @Nullable String getFileExt() {
    return "pcss";
  }

  @Override
  public @NotNull Language getLanguage() {
    return PostCssLanguage.INSTANCE;
  }

  @Override
  public @NotNull CodeStyleConfigurable createConfigurable(@NotNull CodeStyleSettings baseSettings,
                                                           @NotNull CodeStyleSettings modelSettings) {
    return new CodeStyleAbstractConfigurable(baseSettings, modelSettings, getConfigurableDisplayName()) {
      @Override
      protected @NotNull CodeStyleAbstractPanel createPanel(@NotNull CodeStyleSettings settings) {
        return new PostCssStylePanel(getCurrentSettings());
      }
    };
  }

  @Override
  public boolean hasSettingsPage() {
    return false;
  }
}
