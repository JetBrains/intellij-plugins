/*
 * Copyright 2000-2005 JetBrains s.r.o.
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
package org.jetbrains.idea.perforce.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.merge.PerforceMergeProvider;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ResolveAction extends DumbAwareAction {
  @Override
  public void update(@NotNull AnActionEvent e) {
    e.getPresentation().setEnabled(isEnabled(e));
  }

  private static boolean isEnabled(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    if (project == null) return false;

    VirtualFile[] vFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
    if (vFiles == null) return false;

    List<VirtualFile> selectedFiles = getSelectedPerforceFiles(vFiles, project);
    if (selectedFiles.isEmpty() || ContainerUtil.exists(selectedFiles, it -> it.isDirectory())) {
      return false;
    }

    PerforceSettings perforceSettings = PerforceSettings.getSettings(project);
    if (!perforceSettings.ENABLED) {
      return false;
    }

    return !getMergedFiles(project, selectedFiles).isEmpty();
  }

  static boolean serverSupportsConflictsResolve(@NotNull PerforceSettings perforceSettings, @NotNull Collection<P4Connection> connections) {
    try {
      for (P4Connection connection : connections) {
        long serverVersion = perforceSettings.getServerVersionCached(connection);
        if (serverVersion < 2004) return false;
      }
      return true;
    }
    catch (VcsException e) {
      return false;
    }
  }

  private static List<VirtualFile> getSelectedPerforceFiles(VirtualFile @NotNull [] vFiles, @NotNull Project project) {
    return ContainerUtil.filter(vFiles, file -> VcsUtil.getVcsFor(project, file) instanceof PerforceVcs);
  }

  private static List<VirtualFile> getMergedFiles(Project project, List<VirtualFile> selectedFiles) {
    List<VirtualFile> merged = new ArrayList<>();
    for (VirtualFile file : selectedFiles) {
      final FileStatus status = FileStatusManager.getInstance(project).getStatus(file);
      if (status == FileStatus.MERGE || status == FileStatus.MERGED_WITH_CONFLICTS) {
        merged.add(file);
      }
    }
    return merged;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getRequiredData(CommonDataKeys.PROJECT);
    VirtualFile[] vFiles = e.getRequiredData(CommonDataKeys.VIRTUAL_FILE_ARRAY);

    List<VirtualFile> selectedFiles = getSelectedPerforceFiles(vFiles, project);

    PerforceSettings perforceSettings = PerforceSettings.getSettings(project);
    Set<P4Connection> connections = ContainerUtil.map2Set(selectedFiles, file -> perforceSettings.getConnectionForFile(file));
    if (!serverSupportsConflictsResolve(perforceSettings, connections)) {
      Messages.showErrorDialog(project, PerforceBundle.message("message.server.cant.resolve.conflicts"),
                               PerforceBundle.message("message.title.resolve"));
      return;
    }

    new PerforceMergeProvider(project).showMergeDialog(getMergedFiles(project, selectedFiles));
  }
}
