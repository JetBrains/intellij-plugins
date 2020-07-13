// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs.codeStyle;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import com.intellij.prettierjs.PrettierConfig;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import org.jetbrains.annotations.NotNull;

public interface PrettierCodeStyleInstaller {

  ExtensionPointName<PrettierCodeStyleInstaller> EP_NAME = ExtensionPointName.create("com.intellij.prettierjs.codeStyleInstaller");

  void install(@NotNull Project project, @NotNull PrettierConfig config, @NotNull CodeStyleSettings settings);

  boolean isInstalled(@NotNull Project project, @NotNull PrettierConfig config, @NotNull CodeStyleSettings settings);

}
