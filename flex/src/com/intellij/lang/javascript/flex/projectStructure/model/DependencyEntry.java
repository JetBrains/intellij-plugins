package com.intellij.lang.javascript.flex.projectStructure.model;

import org.jetbrains.annotations.NotNull;

public interface DependencyEntry {

  @NotNull
  DependencyType getDependencyType();
}
