package com.intellij.flex.model.bc;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface JpsFlexCompilerOptions extends JpsFlexModuleOrProjectCompilerOptions {

  void setResourceFilesMode(@NotNull ResourceFilesMode resourceFilesMode);

  void setAdditionalOptions(@NotNull String additionalOptions);

  enum ResourceFilesMode {None, All, ResourcePatterns}

  @NotNull
  ResourceFilesMode getResourceFilesMode();

  @NotNull
  Collection<String> getFilesToIncludeInSWC();

  @NotNull
  String getAdditionalConfigFilePath();
}
