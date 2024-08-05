// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.debug;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class LibrarySourcesSearchScope extends GlobalSearchScope {
  private final Collection<VirtualFile> myLibrarySourceRoots;
  private final ProjectFileIndex myIndex;

  LibrarySourcesSearchScope(@NotNull Project project, @NotNull Collection<VirtualFile> librarySourceRoots) {
    super(project);
    myLibrarySourceRoots = librarySourceRoots;
    myIndex = ProjectRootManager.getInstance(project).getFileIndex();
  }

  @Override
  public boolean contains(final @NotNull VirtualFile file) {
    final VirtualFile libSrcRoot = myIndex.getSourceRootForFile(file);
    return libSrcRoot != null && myLibrarySourceRoots.contains(libSrcRoot);
  }

  @Override
  public boolean isSearchInModuleContent(final @NotNull Module aModule) {
    return false;
  }

  @Override
  public boolean isSearchInLibraries() {
    return true;
  }
}
