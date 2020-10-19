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

import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.actions.AbstractVcsAction;
import com.intellij.openapi.vcs.actions.VcsContext;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vcs.ui.Refreshable;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.*;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import java.util.*;

import static com.intellij.util.ArrayUtil.isEmpty;

public class RevertAllUnchangedFilesAction extends AbstractVcsAction {

  @Override
  protected void update(@NotNull VcsContext vcsContext, @NotNull Presentation presentation) {
    final Project project = vcsContext.getProject();
    if (project == null) {
      presentation.setVisible(false);
      return;
    }

    boolean visible;
    Refreshable panel = vcsContext.getRefreshableDialog();
    if (panel instanceof CheckinProjectPanel) {
      visible = ((CheckinProjectPanel)panel).vcsIsAffected(PerforceVcs.NAME);
    }
    else {
      VirtualFile[] files = vcsContext.getSelectedFiles();
      visible = !isEmpty(files) && hasFilesUnderPerforce(Arrays.asList(files), project);
    }
    presentation.setVisible(visible);
    presentation.setEnabled(PerforceSettings.getSettings(project).ENABLED);
  }

  private static Collection<VirtualFile> getSelectedFiles(@NotNull VcsContext vcsContext) {
    Refreshable panel = vcsContext.getRefreshableDialog();
    if (panel instanceof CheckinProjectPanel) {
      return ((CheckinProjectPanel)panel).getRoots();
    }

    VirtualFile[] array = vcsContext.getSelectedFiles();
    if (!isEmpty(array)) {
      return Arrays.asList(array);
    }
    
    return Collections.emptyList();
  }

  private static boolean hasFilesUnderPerforce(final Collection<VirtualFile> roots, Project project) {
    return !findFilesUnderPerforce(roots, project).isEmpty();
  }

  @Override
  protected void actionPerformed(@NotNull VcsContext vcsContext) {
    final CheckinProjectPanel panel = (CheckinProjectPanel)vcsContext.getRefreshableDialog();
    final Collection<VirtualFile> roots = getSelectedFiles(vcsContext);
    final Project project = vcsContext.getProject();

    ApplicationManager.getApplication().runWriteAction(() -> FileDocumentManager.getInstance().saveAllDocuments());

    revertUnchanged(project, roots, panel, vcsContext.getSelectedChangeLists());
  }

  public static void revertUnchanged(final Project project, final Collection<VirtualFile> roots, @Nullable final CheckinProjectPanel panel,
                                     final ChangeList @Nullable [] selection) {
    final List<VcsException> exceptions = new ArrayList<>();

    ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
      if (selection != null && selection.length > 0 && revertChangeLists(project, selection, exceptions)) {
        return;
      }
      revertFiles(roots, project, exceptions);
    }, PerforceBundle.message("message.title.revert.unchanged"), false, project);

    refreshAndDirty(project, roots);
    if (panel != null) {
      panel.refresh();
    }

    if (!exceptions.isEmpty()) {
      AbstractVcsHelper.getInstance(project).showErrors(exceptions, PerforceBundle.message("message.title.revert.unchanged.files"));
    }
  }

  private static void refreshAndDirty(final Project project, Collection<VirtualFile> roots) {
    VfsUtil.markDirty(true, false, roots.toArray(VirtualFile.EMPTY_ARRAY)); // otherwise events from file watcher may come too late
    if (hasDirectories(roots)) {
      VirtualFileManager.getInstance().asyncRefresh(() -> {
        P4File.invalidateFstat(project);
        VcsDirtyScopeManager.getInstance(project).markEverythingDirty();
      });
    }
    else {
      for (final VirtualFile vFile : findFilesUnderPerforce(roots, project)) {
        ApplicationManager.getApplication().runWriteAction(() -> vFile.refresh(false, false));
        P4File.invalidateFstat(vFile);
        if (vFile.isDirectory()) {
          VcsDirtyScopeManager.getInstance(project).dirDirtyRecursively(vFile);
        }
        else {
          VcsDirtyScopeManager.getInstance(project).fileDirty(vFile);
        }
      }
    }
  }

  private static boolean hasDirectories(Collection<VirtualFile> roots) {
    return ContainerUtil.find(roots, file -> file.isDirectory()) != null;
  }

  private static void revertFiles(final Collection<VirtualFile> roots, final Project project, final List<VcsException> exceptions) {
    List<VirtualFile> files = findFilesUnderPerforce(roots, project);
    MultiMap<P4Connection,VirtualFile> map = FileGrouper.distributeFilesByConnection(files, project);
    for (P4Connection connection : map.keySet()) {
      try {
        List<String> paths = ContainerUtil.map2List(map.get(connection), file -> P4File.create(file).getRecursivePath());
        PerforceRunner.getInstance(project).revertUnchanged(connection, paths);
      }
      catch (VcsException e1) {
        exceptions.add(e1);
      }
    }
  }

  private static List<VirtualFile> findFilesUnderPerforce(Collection<VirtualFile> roots, Project project) {
    final PerforceVcs vcs = PerforceVcs.getInstance(project);
    final ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(project);
    return ContainerUtil.findAll(roots, file -> vcsManager.getVcsFor(file) == vcs);
  }

  private static boolean revertChangeLists(final Project project, final ChangeList[] selectedChangeLists, final List<VcsException> exceptions) {
    boolean foundAll = true;
    final PerforceNumberNameSynchronizer synchronizer = PerforceNumberNameSynchronizer.getInstance(project);

    final Collection<P4Connection> connectionList = PerforceSettings.getSettings(project).getAllConnections();

    final Set<Pair<Long, P4Connection>> numbers = new HashSet<>();
    for(P4Connection connection: connectionList) {
      try {
        PerforceManager.ensureValidClient(project, connection);
      }
      catch (VcsException e) {
        exceptions.add(e);
        continue;
      }
      
      final ConnectionKey connectionKey = connection.getConnectionKey();
      for(ChangeList changeList: selectedChangeLists) {
        Long number = synchronizer.getNumber(connectionKey, changeList.getName());
        if (number != null) {
          numbers.add(Pair.create(number, connection));
        }
        else {
          foundAll = false;
        }
      }
    }
    for (Pair<Long, P4Connection> pair : numbers) {
      try {
        PerforceRunner.getInstance(project).revertUnchanged(pair.getSecond(), pair.getFirst().longValue());
      }
      catch (VcsException e1) {
        exceptions.add(e1);
      }
    }
    return foundAll;
  }
}
