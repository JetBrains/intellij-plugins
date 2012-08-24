package com.intellij.flex.model.bc;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface JpsFlexCompilerOptions extends JpsFlexModuleOrProjectCompilerOptions {

  enum ResourceFilesMode {None, All, ResourcePatterns}

  @NotNull
  ResourceFilesMode getResourceFilesMode();

  @NotNull
  Collection<String> getFilesToIncludeInSWC();

  @NotNull
  String getAdditionalConfigFilePath();
}
