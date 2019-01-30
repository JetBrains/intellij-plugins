// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide;

import com.intellij.openapi.fileEditor.impl.NonProjectFileWritingAccessExtension;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartFileType;
import org.jetbrains.annotations.NotNull;

public class DartWritingAccessProvider implements NonProjectFileWritingAccessExtension {

  private final Project myProject;

  public DartWritingAccessProvider(Project project) {
    myProject = project;
  }

  @Override
  public boolean isNotWritable(@NotNull VirtualFile file) {
    return file.getFileType() == DartFileType.INSTANCE && ProjectRootManager.getInstance(myProject).getFileIndex().isExcluded(file);
  }

  @Deprecated
  // TODO remove when Flutter plugin usage is removed
  public static boolean isInDartSdkOrDartPackagesFolder(@NotNull Project project, @NotNull VirtualFile file) {
    return file.getFileType() == DartFileType.INSTANCE && !ProjectFileIndex.getInstance(project).isInContent(file);
  }
}
