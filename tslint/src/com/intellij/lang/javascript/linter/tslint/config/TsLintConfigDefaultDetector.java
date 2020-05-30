// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.linter.tslint.config;

import com.intellij.lang.javascript.linter.JSLinterConfigFileUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.lang.javascript.linter.tslint.TslintUtil.CONFIG_FILE_NAMES;
import static com.intellij.util.ObjectUtils.doIfNotNull;

public class TsLintConfigDefaultDetector implements TsLintConfigDetector {

  @Nullable
  @Override
  public TsLintConfigs detectConfigs(@NotNull Project project, @NotNull VirtualFile fileToBeLinted) {
    return doIfNotNull(JSLinterConfigFileUtil.findFileUpToFileSystemRoot(fileToBeLinted, CONFIG_FILE_NAMES),
                       file -> new TsLintConfigDetector.TsLintConfigs(file, null));
  }
}
