package com.intellij.coldFusion.model.formatter;

import com.intellij.openapi.options.Configurable;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Nadya.Zabrodina
 */
public class CfmlCodeStyleSettingsProvider extends CodeStyleSettingsProvider {
  @NotNull
  @Override
  public Configurable createSettingsPage(CodeStyleSettings settings, CodeStyleSettings originalSettings) {
    return new CfmlCodeStyleConfigurable(settings, originalSettings);
  }

  @Override
  public CustomCodeStyleSettings createCustomSettings(CodeStyleSettings settings) {
    return new CfmlCodeStyleSettings(settings);
  }

  @Override
  public String getConfigurableDisplayName() {
    return "CFML";
  }
}
