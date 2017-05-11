package com.intellij.lang.javascript.flex.projectStructure.model;

import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BuildConfigurationEntry extends DependencyEntry {

  @NotNull
  String getBcName();

  @NotNull
  String getModuleName();

  @Nullable
  Module findModule();

  @Nullable
  FlexBuildConfiguration findBuildConfiguration();
}
