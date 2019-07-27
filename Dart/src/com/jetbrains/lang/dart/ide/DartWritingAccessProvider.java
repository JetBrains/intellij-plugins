// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide;

import com.intellij.openapi.fileEditor.impl.NonProjectFileWritingAccessExtension;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.project.Project;
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
    return FileTypeRegistry.getInstance().isFileOfType(file, DartFileType.INSTANCE) && ProjectRootManager.getInstance(myProject).getFileIndex().isExcluded(file);
  }
}
