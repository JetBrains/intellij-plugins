// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs.codeStyle;

import com.intellij.lang.Language;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import com.intellij.prettierjs.PrettierConfig;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public interface PrettierCodeStyleInstaller {

  ExtensionPointName<PrettierCodeStyleInstaller> EP_NAME = ExtensionPointName.create("com.intellij.prettierjs.codeStyleInstaller");

  void install(@NotNull Project project, @NotNull PrettierConfig config, @NotNull CodeStyleSettings settings);

  boolean isInstalled(@NotNull Project project, @NotNull PrettierConfig config, @NotNull CodeStyleSettings settings);

  static void applyCommonPrettierSettings(@NotNull PrettierConfig config,
                                          @NotNull CodeStyleSettings settings,
                                          @NotNull Language language) {
    CommonCodeStyleSettings commonSettings = settings.getCommonSettings(language);
    CommonCodeStyleSettings.IndentOptions indentOptions = commonSettings.getIndentOptions();
    if (indentOptions != null) {
      indentOptions.INDENT_SIZE = config.tabWidth;
      indentOptions.CONTINUATION_INDENT_SIZE = config.tabWidth;
      indentOptions.TAB_SIZE = config.tabWidth;
      indentOptions.USE_TAB_CHARACTER = config.useTabs;
    }
    settings.setSoftMargins(language, Collections.singletonList(config.printWidth));
  }

  static boolean commonPrettierSettingsApplied(@NotNull PrettierConfig config,
                                               @NotNull CodeStyleSettings settings,
                                               @NotNull Language language) {
    CommonCodeStyleSettings commonSettings = settings.getCommonSettings(language);
    CommonCodeStyleSettings.IndentOptions indentOptions = commonSettings.getIndentOptions();
    List<Integer> softMargins = settings.getSoftMargins(language);
    return (indentOptions == null
            || (indentOptions.INDENT_SIZE == config.tabWidth &&
                indentOptions.TAB_SIZE == config.tabWidth &&
                indentOptions.CONTINUATION_INDENT_SIZE == config.tabWidth &&
                indentOptions.USE_TAB_CHARACTER == config.useTabs))
           && (softMargins.isEmpty()
               || (softMargins.size() == 1 && softMargins.get(0) == config.printWidth));
  }
}
