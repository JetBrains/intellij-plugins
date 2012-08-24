package com.intellij.flex.model.bc;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.library.JpsLibrary;

public interface JpsLibraryDependencyEntry extends JpsFlexDependencyEntry {

  @Nullable
  JpsLibrary getLibrary();
}
