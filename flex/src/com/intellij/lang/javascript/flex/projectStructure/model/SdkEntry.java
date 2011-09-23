package com.intellij.lang.javascript.flex.projectStructure.model;

import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: ksafonov
 */
public interface SdkEntry {
  @NotNull
  String getLibraryId();

  @NotNull
  String getHomePath();

  @Nullable
  LibraryEx findLibrary();
}
