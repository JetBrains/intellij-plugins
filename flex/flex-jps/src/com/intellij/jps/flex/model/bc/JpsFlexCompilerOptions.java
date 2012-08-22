package com.intellij.jps.flex.model.bc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsElement;

import java.util.Collection;
import java.util.Map;

public interface JpsFlexCompilerOptions extends JpsFlexModuleOrProjectCompilerOptions {

  enum ResourceFilesMode {None, All, ResourcePatterns}

  @NotNull
  ResourceFilesMode getResourceFilesMode();

  @NotNull
  Collection<String> getFilesToIncludeInSWC();

  @NotNull
  String getAdditionalConfigFilePath();
}
