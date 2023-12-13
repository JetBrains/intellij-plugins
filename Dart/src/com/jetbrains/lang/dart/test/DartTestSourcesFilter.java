package com.jetbrains.lang.dart.test;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.TestSourcesFilter;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.projectView.DartIconProvider;
import org.jetbrains.annotations.NotNull;

public final class DartTestSourcesFilter extends TestSourcesFilter {
  @Override
  public boolean isTestSource(@NotNull final VirtualFile file, @NotNull final Project project) {
    if (!file.isInLocalFileSystem()) return false;

    final ProjectFileIndex fileIndex = ProjectFileIndex.getInstance(project);
    if (!fileIndex.isInContent(file)) return false;

    if (DartIconProvider.isFolderNearPubspecYaml(file, "test")) return true;

    if (!file.getPath().contains("/test/")) return false; // quick fail

    VirtualFile parent = file;
    while ((parent = parent.getParent()) != null && fileIndex.isInContent(parent)) {
      if (DartIconProvider.isFolderNearPubspecYaml(parent, "test")) return true;
    }

    return false;
  }
}
