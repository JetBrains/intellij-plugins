// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.osmorc.facet;

import com.intellij.openapi.roots.DependencyScope;
import com.intellij.openapi.roots.LibraryDependencyScopeSuggester;
import com.intellij.openapi.roots.libraries.Library;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class OsgiLibraryDependencyScopeSuggester extends LibraryDependencyScopeSuggester {
  @Override
  public @Nullable DependencyScope getDefaultDependencyScope(@NotNull Library library) {
    return OsgiCoreLibraryType.isOsgiCoreLibrary(library) ? DependencyScope.PROVIDED : null;
  }
}
