// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.util;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartBazelFileUtil {

  private static final String WORKSPACE_FILE_NAME = "WORKSPACE";
  private static final String READONLY_FOLDER_NAME = "READONLY";

  @Nullable
  public static VirtualFile getBazelWorkspace(@NotNull final VirtualFile file) {
    VirtualFile parent = file.getParent();
    while (parent != null) {
      final VirtualFile readonlyFolderVFile = parent.findChild(READONLY_FOLDER_NAME);
      if (readonlyFolderVFile != null) {
        return parent;
      }
      final VirtualFile workspaceFile = parent.findChild(WORKSPACE_FILE_NAME);
      if (workspaceFile != null) {
        return parent;
      }
      parent = parent.getParent();
    }
    return null;
  }
}
