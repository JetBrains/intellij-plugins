package com.intellij.lang.javascript.flex.projectStructure.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: ksafonov
 */
public interface Dependencies {

  @Nullable
  SdkEntry getSdkEntry();

  DependencyEntry[] getEntries();

  @NotNull
  LinkageType getFrameworkLinkage();

  @NotNull
  String getTargetPlayer();

  @NotNull
  ComponentSet getComponentSet();
}
