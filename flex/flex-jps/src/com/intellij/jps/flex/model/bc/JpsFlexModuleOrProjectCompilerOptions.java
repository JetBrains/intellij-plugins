package com.intellij.jps.flex.model.bc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsElement;

import java.util.Map;

public interface JpsFlexModuleOrProjectCompilerOptions extends JpsElement {
  @Nullable
  String getOption(@NotNull String name);

  @NotNull
  Map<String, String> getAllOptions();

  @NotNull
  String getAdditionalOptions();
}
