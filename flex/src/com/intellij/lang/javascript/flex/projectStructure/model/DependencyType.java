package com.intellij.lang.javascript.flex.projectStructure.model;

import org.jetbrains.annotations.NotNull;

/**
 * User: ksafonov
 */
public interface DependencyType {
  @NotNull
  LinkageType getLinkageType();

  boolean isEqual(DependencyType other);
}
