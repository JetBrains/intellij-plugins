package com.jetbrains.lang.dart.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PubspecYamlUtil {
  public static final String PUBSPEC_YAML = "pubspec.yaml";

  @Nullable
  public static VirtualFile getPubspecYamlFile(final @NotNull Project project, final @NotNull VirtualFile contextFile) {
    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
    VirtualFile parent = contextFile;
    while ((parent = parent.getParent()) != null && fileIndex.isInContent(parent)) {
      final VirtualFile file = parent.findChild(PUBSPEC_YAML);
      if (file != null) return file;
    }

    return null;
  }

  @Nullable
  public static VirtualFile getDartPackagesFolder(final @NotNull Project project, final @NotNull VirtualFile file) {
    final VirtualFile pubspecYamlFile = getPubspecYamlFile(project, file);
    if (pubspecYamlFile != null) {
      final VirtualFile packagesFolder = pubspecYamlFile.getParent().findChild("packages");
      return packagesFolder != null && packagesFolder.isDirectory() ? packagesFolder : null;
    }
    return null;
  }
}
