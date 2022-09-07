// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.osgi.bnd.imp;

import aQute.bnd.build.Workspace;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class ReimportProjectsAction extends AnAction {
  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    Workspace workspace = BndProjectImporter.getWorkspace(e.getProject());
    boolean available = workspace != null && !getProjectDirs(e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)).isEmpty();
    e.getPresentation().setEnabledAndVisible(available);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    if (project != null) {
      Collection<String> projectDirs = getProjectDirs(e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY));
      if (!projectDirs.isEmpty()) {
        BndProjectImporter.reimportProjects(project, projectDirs);
      }
    }
  }

  private static Collection<String> getProjectDirs(VirtualFile @Nullable [] files) {
    if (files == null || files.length <= 0) {
      return Collections.emptyList();
    }

    Collection<String> projectDirs = new ArrayList<>(files.length);
    for (VirtualFile file : files) {
      if (!file.isInLocalFileSystem()) {
        continue;
      }
      if (!file.isDirectory() && BndProjectImporter.BND_FILE.equals(file.getName())) {
        projectDirs.add(file.getParent().getPath());
      }
      else if (file.isDirectory() && file.findChild(BndProjectImporter.BND_FILE) != null) {
        projectDirs.add(file.getPath());
      }
    }
    return projectDirs;
  }
}
