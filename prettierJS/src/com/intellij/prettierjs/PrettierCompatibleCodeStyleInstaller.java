package com.intellij.prettierjs;

import com.intellij.application.options.CodeStyle;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings;
import com.intellij.openapi.project.Project;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import org.jetbrains.annotations.NotNull;

public class PrettierCompatibleCodeStyleInstaller {

  public static void install(@NotNull Project project, @NotNull PrettierUtil.Config config) {
    CodeStyleSettingsManager settingsManager = CodeStyleSettingsManager.getInstance(project);
    CodeStyleSettings newSettings = settingsManager.getCurrentSettings().clone();
    installJSDialectSettings(newSettings, config, JavascriptLanguage.INSTANCE, JSCodeStyleSettings.class);
    installJSDialectSettings(newSettings, config, JavaScriptSupportLoader.TYPESCRIPT, TypeScriptCodeStyleSettings.class);
    settingsManager.USE_PER_PROJECT_SETTINGS = true;
    settingsManager.setMainProjectCodeStyle(newSettings);
    
  }

  public static boolean isInstalled(@NotNull Project project, @NotNull PrettierUtil.Config config) {
    CodeStyleSettings settings = CodeStyle.getSettings(project);
    return isInstalledForDialect(settings, config, JavascriptLanguage.INSTANCE, JSCodeStyleSettings.class)
           && isInstalledForDialect(settings, config, JavaScriptSupportLoader.TYPESCRIPT, TypeScriptCodeStyleSettings.class);
  }

  private static boolean isInstalledForDialect(CodeStyleSettings settings,
                                               PrettierUtil.Config config,
                                               Language language,
                                               Class<? extends JSCodeStyleSettings> settingsClass) {
    CommonCodeStyleSettings commonSettings = settings.getCommonSettings(language);
    JSCodeStyleSettings customSettings = settings.getCustomSettings(settingsClass);
    CommonCodeStyleSettings.IndentOptions indentOptions = commonSettings.getIndentOptions();
    if (indentOptions == null) {
      return false;
    }
    return indentOptions.INDENT_SIZE == config.tabWidth &&
           indentOptions.CONTINUATION_INDENT_SIZE == config.tabWidth &&
           indentOptions.USE_TAB_CHARACTER == config.useTabs &&
           customSettings.USE_DOUBLE_QUOTES == (!config.singleQuote) &&
           customSettings.USE_SEMICOLON_AFTER_STATEMENT == config.semi &&
           customSettings.SPACES_WITHIN_OBJECT_LITERAL_BRACES == config.bracketSpacing &&
           customSettings.SPACES_WITHIN_OBJECT_TYPE_BRACES == config.bracketSpacing &&
           customSettings.SPACES_WITHIN_IMPORTS == config.bracketSpacing;
  }

  private static void installJSDialectSettings(@NotNull CodeStyleSettings settings,
                                               @NotNull PrettierUtil.Config config,
                                               @NotNull Language language,
                                               @NotNull Class<? extends JSCodeStyleSettings> settingsClass) {
    CommonCodeStyleSettings commonSettings = settings.getCommonSettings(language);
    JSCodeStyleSettings customSettings = settings.getCustomSettings(settingsClass);
    CommonCodeStyleSettings.IndentOptions indentOptions = commonSettings.getIndentOptions();
    if (indentOptions != null) {
      indentOptions.INDENT_SIZE = config.tabWidth;
      indentOptions.CONTINUATION_INDENT_SIZE = config.tabWidth;
      indentOptions.TAB_SIZE = config.tabWidth;
      indentOptions.USE_TAB_CHARACTER = config.useTabs;
    }
    customSettings.USE_DOUBLE_QUOTES = !config.singleQuote;
    customSettings.USE_SEMICOLON_AFTER_STATEMENT = config.semi;
    customSettings.SPACES_WITHIN_OBJECT_LITERAL_BRACES = config.bracketSpacing;
    customSettings.SPACES_WITHIN_OBJECT_TYPE_BRACES = config.bracketSpacing;
    customSettings.SPACES_WITHIN_IMPORTS = config.bracketSpacing;
    
    customSettings.SPACE_BEFORE_FUNCTION_LEFT_PARENTH = false; 
  }
}
