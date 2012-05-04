package com.intellij.lang.javascript.flex.projectStructure.model;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ModifiableAirPackagingOptions extends AirPackagingOptions {

  void setUseGeneratedDescriptor(boolean useGeneratedDescriptor);

  void setCustomDescriptorPath(@NotNull String customDescriptorPath);

  void setPackageFileName(@NotNull String packageFileName);

  void setFilesToPackage(@NotNull List<FilePathAndPathInPackage> filesToPackage);

  void setSigningOptions(@NotNull AirSigningOptions signingOptions);
}
