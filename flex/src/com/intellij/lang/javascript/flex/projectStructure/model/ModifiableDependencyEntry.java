package com.intellij.lang.javascript.flex.projectStructure.model;

import org.jetbrains.annotations.NotNull;

/**
 * User: ksafonov
 */
public interface ModifiableDependencyEntry extends DependencyEntry {

  @NotNull
  @Override
  ModifiableDependencyType getDependencyType();

  boolean isEqual(ModifiableDependencyEntry other);
}
