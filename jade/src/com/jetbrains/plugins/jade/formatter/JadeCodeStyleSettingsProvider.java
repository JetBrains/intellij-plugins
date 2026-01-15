// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.formatter;

import com.intellij.application.options.CodeStyleAbstractConfigurable;
import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.openapi.options.Configurable;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider;
import com.jetbrains.plugins.jade.JadeLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class JadeCodeStyleSettingsProvider extends CodeStyleSettingsProvider {
  @Override
  public @NotNull Configurable createSettingsPage(@NotNull CodeStyleSettings settings, final @NotNull CodeStyleSettings originalSettings) {
    return new CodeStyleAbstractConfigurable(settings, originalSettings, JadeLanguage.INSTANCE.getDisplayName()) {
      @Override
      protected @NotNull CodeStyleAbstractPanel createPanel(@NotNull CodeStyleSettings settings) {
        return new JadeCodeStyleMainPanel(settings, originalSettings);
      }

      @Override
      public @Nullable String getHelpTopic() {
        return "reference.settingsdialog.codestyle.jade";
      }
    };
  }

  @Override
  public @NotNull String getConfigurableId() {
    return "preferences.sourceCode.Pug/Jade";
  }

  @Override
  public @Nullable String getConfigurableDisplayName() {
    return JadeLanguage.INSTANCE.getDisplayName();
  }

}
