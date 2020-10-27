// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs.codeStyle;

import com.intellij.lang.Language;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings;
import com.intellij.openapi.project.Project;
import com.intellij.prettierjs.PrettierConfig;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import org.jetbrains.annotations.NotNull;

import static com.intellij.prettierjs.codeStyle.PrettierCodeStyleInstaller.applyCommonPrettierSettings;
import static com.intellij.prettierjs.codeStyle.PrettierCodeStyleInstaller.commonPrettierSettingsApplied;

public class JSPrettierCodeStyleInstaller implements PrettierCodeStyleInstaller {
  @Override
  public void install(@NotNull Project project, @NotNull PrettierConfig config, @NotNull CodeStyleSettings settings) {
    installJSDialectSettings(config, settings, JavascriptLanguage.INSTANCE, JSCodeStyleSettings.class);
    installJSDialectSettings(config, settings, JavaScriptSupportLoader.TYPESCRIPT, TypeScriptCodeStyleSettings.class);
  }

  @Override
  public boolean isInstalled(@NotNull Project project, @NotNull PrettierConfig config, @NotNull CodeStyleSettings settings) {
    return isInstalledForDialect(config, settings, JavascriptLanguage.INSTANCE, JSCodeStyleSettings.class)
           && isInstalledForDialect(config, settings, JavaScriptSupportLoader.TYPESCRIPT, TypeScriptCodeStyleSettings.class);
  }


  private static boolean isInstalledForDialect(@NotNull PrettierConfig config,
                                               CodeStyleSettings settings,
                                               Language language,
                                               Class<? extends JSCodeStyleSettings> settingsClass) {
    JSCodeStyleSettings customSettings = settings.getCustomSettings(settingsClass);
    return customSettings.USE_DOUBLE_QUOTES == (!config.singleQuote) &&
           customSettings.FORCE_QUOTE_STYlE &&
           customSettings.USE_SEMICOLON_AFTER_STATEMENT == config.semi &&
           customSettings.FORCE_SEMICOLON_STYLE &&
           customSettings.SPACES_WITHIN_OBJECT_LITERAL_BRACES == config.bracketSpacing &&
           customSettings.SPACES_WITHIN_OBJECT_TYPE_BRACES == config.bracketSpacing &&
           customSettings.SPACES_WITHIN_IMPORTS == config.bracketSpacing &&
           customSettings.ENFORCE_TRAILING_COMMA == convertTrailingCommaOption(config.trailingComma) &&
           commonPrettierSettingsApplied(config, settings, language);
  }

  private static void installJSDialectSettings(@NotNull PrettierConfig config,
                                               @NotNull CodeStyleSettings settings,
                                               @NotNull Language language,
                                               @NotNull Class<? extends JSCodeStyleSettings> settingsClass) {
    applyCommonPrettierSettings(config, settings, language);

    JSCodeStyleSettings customSettings = settings.getCustomSettings(settingsClass);
    customSettings.USE_DOUBLE_QUOTES = !config.singleQuote;
    customSettings.USE_SEMICOLON_AFTER_STATEMENT = config.semi;
    customSettings.SPACES_WITHIN_OBJECT_LITERAL_BRACES = config.bracketSpacing;
    customSettings.SPACES_WITHIN_OBJECT_TYPE_BRACES = config.bracketSpacing;
    customSettings.SPACES_WITHIN_IMPORTS = config.bracketSpacing;
    customSettings.ENFORCE_TRAILING_COMMA = convertTrailingCommaOption(config.trailingComma);

    // Default prettier settings
    customSettings.FORCE_QUOTE_STYlE = true;
    customSettings.FORCE_SEMICOLON_STYLE = true;
    customSettings.SPACE_BEFORE_FUNCTION_LEFT_PARENTH = false;
  }

  @NotNull
  private static JSCodeStyleSettings.TrailingCommaOption convertTrailingCommaOption(@NotNull PrettierConfig.TrailingCommaOption option) {
    switch (option) {
      case none:
        return JSCodeStyleSettings.TrailingCommaOption.Remove;
      case all:
      case es5:
        return JSCodeStyleSettings.TrailingCommaOption.WhenMultiline;
    }
    return JSCodeStyleSettings.TrailingCommaOption.Remove;
  }
}
