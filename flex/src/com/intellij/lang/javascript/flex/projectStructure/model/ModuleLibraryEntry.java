package com.intellij.lang.javascript.flex.projectStructure.model;

import org.jetbrains.annotations.NotNull;

public interface ModuleLibraryEntry extends DependencyEntry {

  @NotNull
  String getLibraryId();
}
