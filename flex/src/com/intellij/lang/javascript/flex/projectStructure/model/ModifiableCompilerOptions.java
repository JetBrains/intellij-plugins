package com.intellij.lang.javascript.flex.projectStructure.model;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface ModifiableCompilerOptions extends CompilerOptions {
  void setAllOptions(Map<String, String> newOptions);

  void setResourceFilesMode(@NotNull ResourceFilesMode mode);

  void setAdditionalConfigFilePath(@NotNull String path);

  void setAdditionalOptions(@NotNull String options);
}
