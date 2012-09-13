package com.intellij.lang.javascript.flex.projectStructure.model;

import org.jetbrains.annotations.NotNull;

public interface IosPackagingOptions extends AirPackagingOptions {

  boolean isEnabled();

  @NotNull
  String getIOSSdkPath();
}
