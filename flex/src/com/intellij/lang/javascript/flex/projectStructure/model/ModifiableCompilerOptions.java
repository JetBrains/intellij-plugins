package com.intellij.lang.javascript.flex.projectStructure.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface ModifiableCompilerOptions extends CompilerOptions {
  void setOption(@NotNull String name, @Nullable String value);

  void setAllOptions(Map<String, String> newOptions);
}
