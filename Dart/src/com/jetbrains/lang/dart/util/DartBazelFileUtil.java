// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.util;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DartBazelFileUtil {

  private static final String WORKSPACE_FILE_NAME = "WORKSPACE";
  private static final String READONLY_FOLDER_NAME = "READONLY";

  public static @Nullable VirtualFile getBazelWorkspace(final @NotNull VirtualFile file) {
    VirtualFile parent = file.getParent();
    while (parent != null) {
      final VirtualFile readonlyFolderVFile = parent.findChild(READONLY_FOLDER_NAME);
      if (readonlyFolderVFile != null && readonlyFolderVFile.isDirectory()) {
        return parent;
      }
      final VirtualFile workspaceFile = parent.findChild(WORKSPACE_FILE_NAME);
      if (workspaceFile != null && !workspaceFile.isDirectory()) {
        return parent;
      }
      parent = parent.getParent();
    }
    return null;
  }
}
