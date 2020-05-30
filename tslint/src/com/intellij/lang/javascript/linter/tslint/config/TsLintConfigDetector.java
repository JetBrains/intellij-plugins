// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.linter.tslint.config;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TsLintConfigDetector {

  ExtensionPointName<TsLintConfigDetector> TS_LINT_CONFIG_DETECTOR_EP =
    ExtensionPointName.create("com.intellij.tslint.configDetector");

  @Nullable
  TsLintConfigs detectConfigs(@NotNull Project project, @NotNull VirtualFile fileToBeLinted);

  final class TsLintConfigs {

    @NotNull private final VirtualFile myTsLintConfig;
    @Nullable private final VirtualFile myTsConfig;

    public TsLintConfigs(@NotNull VirtualFile tsLintConfig, @Nullable VirtualFile tsConfig) {
      myTsLintConfig = tsLintConfig;
      myTsConfig = tsConfig;
    }

    @NotNull
    public VirtualFile getTsLintConfig() {
      return myTsLintConfig;
    }

    @Nullable
    public VirtualFile getTsConfig() {
      return myTsConfig;
    }
  }
}
