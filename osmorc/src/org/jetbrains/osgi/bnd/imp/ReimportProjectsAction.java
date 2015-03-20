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
package org.jetbrains.osgi.bnd.imp;

import aQute.bnd.build.Workspace;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public class ReimportProjectsAction extends AnAction {
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

  private static Collection<String> getProjectDirs(@Nullable VirtualFile[] files) {
    if (files == null || files.length <= 0) {
      return Collections.emptyList();
    }

    Collection<String> projectDirs = ContainerUtil.newArrayListWithCapacity(files.length);
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
