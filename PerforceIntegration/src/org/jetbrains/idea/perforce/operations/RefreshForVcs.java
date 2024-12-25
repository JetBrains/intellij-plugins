// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.idea.perforce.operations;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.NewVirtualFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Refreshes VFS; refreshes VCS statuses as needed
 */
public class RefreshForVcs {
  private final List<File> myFiles = new ArrayList<>();
  private final List<File> myDirs = new ArrayList<>();

  public void refreshFile(final File file) {
    myFiles.add(file);
  }

  public void addDeletedFile(final File file) {
    // to be refreshed recursively for simplicity
    myDirs.add(file.getParentFile());
  }

  public void run(final Project project) {
    LocalFileSystem.getInstance().refreshIoFiles(myFiles);
    LocalFileSystem.getInstance().refreshIoFiles(myDirs);

    List<VirtualFile> vFiles = new ArrayList<>();
    for (File file : myFiles) {
      ContainerUtil.addIfNotNull(vFiles, LocalFileSystem.getInstance().findFileByIoFile(file));
    }

    List<VirtualFile> vDirs = new ArrayList<>();
    for (File dir : myDirs) {
      ContainerUtil.addIfNotNull(vDirs, refreshDir(dir));
    }

    VcsDirtyScopeManager.getInstance(project).filesDirty(vFiles, vDirs);
  }

  private static VirtualFile refreshDir(final @Nullable File dir) {
    if (dir == null) return null;

    final VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(dir);
    if (vf == null) {
      return refreshDir(dir.getParentFile());
    }
    ((NewVirtualFile) vf).markDirtyRecursively();
    vf.refresh(false, true);
    return vf;
  }
}
