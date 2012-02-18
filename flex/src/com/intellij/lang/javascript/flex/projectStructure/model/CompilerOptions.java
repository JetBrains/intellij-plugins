package com.intellij.lang.javascript.flex.projectStructure.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface CompilerOptions {
  enum ResourceFilesMode {None, All, ResourcePatterns}

  @Nullable
  String getOption(@NotNull String name);

  Map<String, String> getAllOptions();

  @NotNull
  ResourceFilesMode getResourceFilesMode();

  @NotNull
  String getAdditionalConfigFilePath();

  @NotNull
  String getAdditionalOptions();
}
