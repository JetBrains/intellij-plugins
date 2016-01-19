package com.jetbrains.lang.dart.ide.formatter.settings;

import com.intellij.lang.Language;
import com.intellij.openapi.options.Configurable;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider;
import com.intellij.psi.codeStyle.DisplayPriority;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartLanguage;
import org.jetbrains.annotations.NotNull;

/**
 * @author: Fedor.Korotkov
 */
public class DartCodeStyleSettingsProvider extends CodeStyleSettingsProvider {
  @NotNull
  @Override
  public Configurable createSettingsPage(CodeStyleSettings settings, CodeStyleSettings originalSettings) {
    return new DartCodeStyleConfigurable(settings, originalSettings);
  }

  @Override
  public String getConfigurableDisplayName() {
    return DartBundle.message("dart.title");
  }

  @Override
  public DisplayPriority getPriority() {
    return DisplayPriority.LANGUAGE_SETTINGS;
  }

  @Override
  public Language getLanguage() {
    return DartLanguage.INSTANCE;
  }
}
