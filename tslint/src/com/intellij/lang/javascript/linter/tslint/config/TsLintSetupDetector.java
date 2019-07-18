// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.linter.tslint.config;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TsLintSetupDetector {

  ExtensionPointName<TsLintSetupDetector> TS_LINT_SETUP_DETECTOR_EP =
    ExtensionPointName.create("com.intellij.tslint.setupDetector");

  @Nullable
  TsLintSetup detectSetup(@NotNull Project project, @NotNull VirtualFile fileToBeLinted);

  static TsLintSetupBuilder builder(@NotNull VirtualFile tsLintConfig) {
    return new TsLintSetupBuilder(tsLintConfig);
  }

  final class TsLintSetup {

    @NotNull private final VirtualFile myTsLintConfig;
    @Nullable private final VirtualFile myTsConfig;
    @Nullable private final String myFormat;

    private TsLintSetup(@NotNull VirtualFile tsLintConfig, @Nullable VirtualFile tsConfig, @Nullable String format) {
      myTsLintConfig = tsLintConfig;
      myTsConfig = tsConfig;
      myFormat = format;
    }

    @NotNull
    public VirtualFile getTsLintConfig() {
      return myTsLintConfig;
    }

    @Nullable
    public VirtualFile getTsConfig() {
      return myTsConfig;
    }

    @Nullable
    public String getFormat() {
      return myFormat;
    }
  }

  final class TsLintSetupBuilder {

    @NotNull private final VirtualFile myTsLintConfig;
    @Nullable private VirtualFile myTsConfig;
    @Nullable private String myFormat;

    private TsLintSetupBuilder(@NotNull VirtualFile config) {
      myTsLintConfig = config;
    }

    public TsLintSetupBuilder setTsConfig(@Nullable VirtualFile tsConfig) {
      myTsConfig = tsConfig;
      return this;
    }

    public TsLintSetupBuilder setFormat(@Nullable String format) {
      myFormat = format;
      return this;
    }

    public TsLintSetup build() {
      return new TsLintSetup(myTsLintConfig, myTsConfig, myFormat);
    }
  }
}
