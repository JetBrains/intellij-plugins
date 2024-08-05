// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.lang.javascript.flex.projectStructure.model.CompilerOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

public class NonStructuralModifiableCompilerOptions implements CompilerOptions {
  private final CompilerOptionsImpl myOriginal;

  NonStructuralModifiableCompilerOptions(final CompilerOptionsImpl compilerOptions) {
    myOriginal = compilerOptions;
  }

  @Override
  public @NotNull ResourceFilesMode getResourceFilesMode() {
    return myOriginal.getResourceFilesMode();
  }

  @Override
  public @Nullable String getOption(final @NotNull String name) {
    return myOriginal.getOption(name);
  }

  @Override
  public @NotNull String getAdditionalConfigFilePath() {
    return myOriginal.getAdditionalConfigFilePath();
  }

  @Override
  public Map<String, String> getAllOptions() {
    return myOriginal.getAllOptions();
  }

  @Override
  public Collection<String> getFilesToIncludeInSWC() {
    return myOriginal.getFilesToIncludeInSWC();
  }

  public void setAdditionalConfigFilePath(final @NotNull String path) {
    // TODO is this really non-structural? should we restart highlighting?
    myOriginal.setAdditionalConfigFilePath(path);
  }

  @Override
  public @NotNull String getAdditionalOptions() {
    return myOriginal.getAdditionalOptions();
  }

}
