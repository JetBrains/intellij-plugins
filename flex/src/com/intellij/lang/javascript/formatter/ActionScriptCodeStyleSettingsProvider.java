package com.intellij.lang.javascript.formatter;

import com.intellij.lang.Language;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.openapi.options.Configurable;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Rustam Vishnyakov
 */
public class ActionScriptCodeStyleSettingsProvider extends CodeStyleSettingsProvider {

  @Override
  public CustomCodeStyleSettings createCustomSettings(CodeStyleSettings settings) {
    return new ECMA4CodeStyleSettings(settings);
  }

  @Nullable
  @Override
  public Language getLanguage() {
    return JavaScriptSupportLoader.ECMA_SCRIPT_L4;
  }

  @Override
  public String getConfigurableDisplayName() {
    return "ActionScript";
  }

  @NotNull
  @Override
  public Configurable createSettingsPage(CodeStyleSettings settings, CodeStyleSettings originalSettings) {
    return new ActionScriptCodeStyleSettingsConfigurable(settings, originalSettings);
  }
}
