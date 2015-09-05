package com.intellij.lang.javascript.flex.projectStructure.model;

public interface ModifiableIosPackagingOptions extends IosPackagingOptions, ModifiableAirPackagingOptions {

  void setEnabled(boolean enabled);
}
