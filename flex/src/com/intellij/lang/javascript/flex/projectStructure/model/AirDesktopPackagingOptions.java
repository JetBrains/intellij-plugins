package com.intellij.lang.javascript.flex.projectStructure.model;

import org.jetbrains.annotations.NotNull;

public interface AirDesktopPackagingOptions extends AirPackagingOptions {

  boolean isUseGeneratedDescriptor();

  @NotNull
  String getCustomDescriptorPath();

  @NotNull
  String getInstallerFileName();
}
