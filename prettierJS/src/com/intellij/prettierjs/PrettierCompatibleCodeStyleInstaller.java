// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs;

import com.intellij.application.options.CodeStyle;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.lang.javascript.formatter.JSCodeStyleUtil;
import com.intellij.lang.javascript.linter.JSLinterUtil;
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAwareRunnable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.DirectoryProjectConfigurator;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PrettierCompatibleCodeStyleInstaller implements DirectoryProjectConfigurator {

  @Override
  public void configureProject(@NotNull Project project, @NotNull VirtualFile baseDir, @NotNull Ref<Module> moduleRef) {
    if (project.isDefault() || project.getBaseDir() == null || project.isDisposed()) return;
    StartupManager.getInstance(project).runWhenProjectIsInitialized((DumbAwareRunnable)() -> installCodeStyle(project));
  }

  private static void installCodeStyle(@NotNull Project project) {
    VirtualFile aConfig = PrettierUtil.findSingleConfigInContentRoots(project);
    if (aConfig != null) {
      install(project, aConfig, false);
    }
  }

  public static void install(@NotNull Project project, @NotNull VirtualFile virtualFile, boolean reportAlreadyImported) {
    PrettierUtil.Config config = PrettierUtil.parseConfig(project, virtualFile);
    if (config != null) {
      install(project, virtualFile, config, reportAlreadyImported);
    }
  }

  public static void install(@NotNull Project project, @NotNull VirtualFile configFile,
                             @NotNull PrettierUtil.Config config, boolean reportAlreadyImported) {
    if (isInstalled(project, config)) {
      if (reportAlreadyImported) {
        JSLinterUtil.reportCodeStyleSettingsAlreadyImported(project, "Prettier");
      }
      return;
    }
    install(project, config);
    PrettierNotificationUtil.reportCodeStyleSettingsImported(project, configFile, null);
  }

  private static void install(@NotNull Project project, @NotNull PrettierUtil.Config config) {
    JSCodeStyleUtil.updateProjectCodeStyle(project, newSettings -> {
      newSettings.LINE_SEPARATOR = config.lineSeparator;
      installJSDialectSettings(newSettings, config, JavascriptLanguage.INSTANCE, JSCodeStyleSettings.class);
      installJSDialectSettings(newSettings, config, JavaScriptSupportLoader.TYPESCRIPT, TypeScriptCodeStyleSettings.class);
    });
  }

  public static boolean isInstalled(@NotNull Project project, @NotNull PrettierUtil.Config config) {
    CodeStyleSettings settings = CodeStyle.getSettings(project);
    return isInstalledForDialect(settings, config, JavascriptLanguage.INSTANCE, JSCodeStyleSettings.class)
           && isInstalledForDialect(settings, config, JavaScriptSupportLoader.TYPESCRIPT, TypeScriptCodeStyleSettings.class)
           && StringUtil.equals(settings.LINE_SEPARATOR, config.lineSeparator);
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
    List<Integer> softMargins = settings.getSoftMargins(language);
    return indentOptions.INDENT_SIZE == config.tabWidth &&
           indentOptions.CONTINUATION_INDENT_SIZE == config.tabWidth &&
           indentOptions.USE_TAB_CHARACTER == config.useTabs &&
           customSettings.USE_DOUBLE_QUOTES == (!config.singleQuote) &&
           softMargins.size() == 1 && softMargins.get(0) == config.printWidth &&
           customSettings.FORCE_QUOTE_STYlE &&
           customSettings.USE_SEMICOLON_AFTER_STATEMENT == config.semi &&
           customSettings.FORCE_SEMICOLON_STYLE &&
           customSettings.SPACES_WITHIN_OBJECT_LITERAL_BRACES == config.bracketSpacing &&
           customSettings.SPACES_WITHIN_OBJECT_TYPE_BRACES == config.bracketSpacing &&
           customSettings.SPACES_WITHIN_IMPORTS == config.bracketSpacing &&
           customSettings.ENFORCE_TRAILING_COMMA == convertTrailingCommaOption(config.trailingComma);
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
    customSettings.FORCE_QUOTE_STYlE = true;
    customSettings.USE_SEMICOLON_AFTER_STATEMENT = config.semi;
    customSettings.FORCE_SEMICOLON_STYLE = true;
    customSettings.SPACES_WITHIN_OBJECT_LITERAL_BRACES = config.bracketSpacing;
    customSettings.SPACES_WITHIN_OBJECT_TYPE_BRACES = config.bracketSpacing;
    customSettings.SPACES_WITHIN_IMPORTS = config.bracketSpacing;
    customSettings.ENFORCE_TRAILING_COMMA = convertTrailingCommaOption(config.trailingComma);

    customSettings.SPACE_BEFORE_FUNCTION_LEFT_PARENTH = false;
    settings.setSoftMargins(language, ContainerUtil.list(config.printWidth));
  }

  @NotNull
  private static JSCodeStyleSettings.TrailingCommaOption convertTrailingCommaOption(@NotNull PrettierUtil.TrailingCommaOption option) {
    switch (option) {
      case none:
        return JSCodeStyleSettings.TrailingCommaOption.Remove;
      case all:
        return JSCodeStyleSettings.TrailingCommaOption.WhenMultiline;
      case es5:
        return JSCodeStyleSettings.TrailingCommaOption.WhenMultiline;
    }
    return JSCodeStyleSettings.TrailingCommaOption.Remove;
  }
}
