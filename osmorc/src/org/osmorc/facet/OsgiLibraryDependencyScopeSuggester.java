package org.osmorc.facet;

import com.intellij.openapi.roots.DependencyScope;
import com.intellij.openapi.roots.LibraryDependencyScopeSuggester;
import com.intellij.openapi.roots.libraries.Library;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class OsgiLibraryDependencyScopeSuggester extends LibraryDependencyScopeSuggester {
  @Nullable
  @Override
  public DependencyScope getDefaultDependencyScope(@NotNull Library library) {
    return OsgiCoreLibraryType.isOsgiCoreLibrary(library) ? DependencyScope.PROVIDED : null;
  }
}
