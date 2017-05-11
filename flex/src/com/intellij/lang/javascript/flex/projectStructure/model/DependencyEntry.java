package com.intellij.lang.javascript.flex.projectStructure.model;

import com.intellij.lang.javascript.flex.projectStructure.model.DependencyType;
import org.jetbrains.annotations.NotNull;

public interface DependencyEntry {

  @NotNull
  DependencyType getDependencyType();
}
