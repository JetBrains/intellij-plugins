// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.test;

import com.intellij.openapi.project.BaseProjectDirectories;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.TestSourcesFilter;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.projectView.DartIconProvider;
import org.jetbrains.annotations.NotNull;

public final class DartTestSourcesFilter extends TestSourcesFilter {
  @Override
  public boolean isTestSource(final @NotNull VirtualFile file, final @NotNull Project project) {
    if (!file.isInLocalFileSystem()) return false;

    VirtualFile baseDir = BaseProjectDirectories.getInstance(project).getBaseDirectoryFor(file);
    if (baseDir == null) return false;

    if (DartIconProvider.isFolderNearPubspecYaml(file, "test")) return true;

    if (!file.getPath().contains("/test/")) return false; // quick fail

    String pathStart = baseDir.getPath() + "/";
    VirtualFile parent = file;
    while ((parent = parent.getParent()) != null && parent.getPath().startsWith(pathStart)) {
      if (DartIconProvider.isFolderNearPubspecYaml(parent, "test")) return true;
    }

    return false;
  }
}
