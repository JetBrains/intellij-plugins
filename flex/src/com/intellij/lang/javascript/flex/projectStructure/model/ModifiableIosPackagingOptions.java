package com.intellij.lang.javascript.flex.projectStructure.model;

import org.jetbrains.annotations.NotNull;

public interface ModifiableIosPackagingOptions extends IosPackagingOptions, ModifiableAirPackagingOptions {

  void setEnabled(boolean enabled);

  void setIOSSdkPath(@NotNull String iosSdkPath);
}
