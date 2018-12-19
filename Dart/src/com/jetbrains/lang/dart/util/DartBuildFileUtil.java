// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.ide.errorTreeView.DartProblemsView;
import com.jetbrains.lang.dart.ide.errorTreeView.DartProblemsViewSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartBuildFileUtil {

  public static final String BUILD_FILE_NAME = "BUILD";
  public static final String LIB_DIR_NAME = "lib";

  /**
   * Return the BUILD build in the root of the package that contains the given context file.
   * <p>
   * This may be not the closest BUILD file.
   * For example it will ignore "examples/BUILD" file, because the enclosing folder contains a "lib" folder and another BUILD file.
   */
  @Nullable
  public static VirtualFile findPackageRootBuildFile(@NotNull final Project project, @NotNull final VirtualFile contextFile) {
    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
    VirtualFile parent = contextFile.isDirectory() ? contextFile : contextFile.getParent();

    boolean isPackageScopedAnalysis =
      DartProblemsView.getInstance(project).getScopeAnalysisMode() == DartProblemsViewSettings.ScopedAnalysisMode.DartPackage;

    while (parent != null && (isPackageScopedAnalysis || fileIndex.isInContent(parent))) {
      final VirtualFile file = parent.findChild(BUILD_FILE_NAME);
      if (file != null && !file.isDirectory()) {
        final VirtualFile parent2 = parent.getParent();
        if (parent2 != null && parent2.findChild(LIB_DIR_NAME) == null) {
          return file;
        }
      }
      parent = parent.getParent();
    }
    return null;
  }

  @NotNull
  public static String getDartProjectName(@NotNull final VirtualFile buildFile) {
    return buildFile.getParent().getName();
  }
}
