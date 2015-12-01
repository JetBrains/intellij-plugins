/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.lang.dart.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartBuildFileUtil {

  public static final String BUILD_FILE_NAME = "BUILD";
  public static final String LIB_DIR_NAME = "lib";

  /**
   * Return the BUILD build in the root of the package that contains the given context file.
   *
   * This may be not the closest BUILD file.
   * For example it will ignore "examples/BUILD" file, because the enclosing folder contains a "lib" folder and another BUILD file.
   */
  @Nullable
  public static VirtualFile findPackageRootBuildFile(@NotNull final Project project, @NotNull final VirtualFile contextFile) {
    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
    VirtualFile parent = contextFile.isDirectory() ? contextFile : contextFile.getParent();

    while (parent != null && fileIndex.isInContent(parent)) {
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

  @Nullable
  public static String getDartProjectName(@NotNull final VirtualFile buildFile) {
    return buildFile.getParent().getName();
  }
}
