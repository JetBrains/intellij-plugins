// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs.codeStyle;

import com.intellij.openapi.project.Project;
import com.intellij.prettierjs.PrettierConfig;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class CommonPrettierCodeStyleInstaller implements PrettierCodeStyleInstaller {

  @Override
  public void install(@NotNull Project project, @NotNull PrettierConfig config, @NotNull CodeStyleSettings settings) {
    CommonCodeStyleSettings.IndentOptions indentOptions = settings.getIndentOptions();
    indentOptions.INDENT_SIZE = config.tabWidth;
    indentOptions.TAB_SIZE = config.tabWidth;
    indentOptions.CONTINUATION_INDENT_SIZE = config.tabWidth;
    indentOptions.USE_TAB_CHARACTER = config.useTabs;
    settings.setDefaultSoftMargins(Collections.singletonList(config.printWidth));
  }

  @Override
  public boolean isInstalled(@NotNull Project project, @NotNull PrettierConfig config, @NotNull CodeStyleSettings settings) {
    CommonCodeStyleSettings.IndentOptions indentOptions = settings.getIndentOptions();
    List<Integer> softMargins = settings.getDefaultSoftMargins();
    return indentOptions.INDENT_SIZE == config.tabWidth &&
           indentOptions.TAB_SIZE == config.tabWidth &&
           indentOptions.CONTINUATION_INDENT_SIZE == config.tabWidth &&
           indentOptions.USE_TAB_CHARACTER == config.useTabs &&
           softMargins.size() == 1 && softMargins.get(0) == config.printWidth;
  }
}
