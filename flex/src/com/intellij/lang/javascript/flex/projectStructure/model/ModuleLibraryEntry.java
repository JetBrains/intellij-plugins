package com.intellij.lang.javascript.flex.projectStructure.model;

import org.jetbrains.annotations.NotNull;

/**
 * User: ksafonov
 */
public interface ModuleLibraryEntry extends DependencyEntry {

  @NotNull
  String getLibraryId();
}
