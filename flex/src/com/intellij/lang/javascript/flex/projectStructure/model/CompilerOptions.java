package com.intellij.lang.javascript.flex.projectStructure.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface CompilerOptions {
  @Nullable
  String getOption(@NotNull String name);

  Map<String, String> getAllOptions();

  @NotNull
  String getAdditionalConfigFilePath();

  @NotNull
  String getAdditionalOptions();
}
