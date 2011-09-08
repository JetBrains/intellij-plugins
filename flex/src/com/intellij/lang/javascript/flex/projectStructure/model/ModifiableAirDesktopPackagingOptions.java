package com.intellij.lang.javascript.flex.projectStructure.model;

import org.jetbrains.annotations.NotNull;

public interface ModifiableAirDesktopPackagingOptions extends AirDesktopPackagingOptions {
  void setUseGeneratedDescriptor(boolean useGeneratedDescriptor);

  void setCustomDescriptorPath(@NotNull String customDescriptorPath);

  void setInstallerFileName(@NotNull String installerFileName);
}
