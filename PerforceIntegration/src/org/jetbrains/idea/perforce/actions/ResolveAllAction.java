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
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsRunnable;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.merge.PerforceMergeProvider;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ResolveAllAction extends DumbAwareAction {
  @Override
  public void update(@NotNull AnActionEvent e) {
    Presentation presentation = e.getPresentation();

    Project project = e.getProject();
    if (project == null) {
      presentation.setEnabledAndVisible(false);
      return;
    }

    AbstractVcs[] allActiveVcss = ProjectLevelVcsManager.getInstance(project).getAllActiveVcss();
    boolean found = false;
    for (AbstractVcs vcs : allActiveVcss) {
      if (PerforceVcs.NAME.equals(vcs.getName())) {
        found = true;
        break;
      }
    }
    if (!found) {
      presentation.setEnabledAndVisible(false);
      return;
    }

    PerforceSettings settings = PerforceSettings.getSettings(project);
    if (!settings.ENABLED) {
      presentation.setEnabled(false);
      presentation.setVisible(true);
      return;
    }

    presentation.setEnabledAndVisible(true);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getRequiredData(CommonDataKeys.PROJECT);

    PerforceSettings settings = PerforceSettings.getSettings(project);
    if (!ResolveAction.serverSupportsConflictsResolve(settings, settings.getAllConnections())) {
      Messages.showErrorDialog(project, PerforceBundle.message("message.server.cant.resolve.conflicts"),
                               PerforceBundle.message("message.title.resolve"));
      return;
    }

    try {
      List<VirtualFile> filesToResolveUnderProject = new ArrayList<>();
      PerforceVcs vcs = PerforceVcs.getInstance(project);
      PerforceRunner runner = PerforceRunner.getInstance(project);
      ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(project);

      VcsUtil.runVcsProcessWithProgress(new VcsRunnable() {
        @Override
        public void run() throws VcsException {
          Collection<Pair<P4Connection, Collection<VirtualFile>>> rootsByConnections = vcs.getRootsByConnections();
          for (Pair<P4Connection, Collection<VirtualFile>> pair : rootsByConnections) {
            for (VirtualFile root : pair.getSecond()) {
              for (VirtualFile file : runner.getResolvedWithConflicts(pair.getFirst(), root)) {
                if (vcsManager.getVcsFor(file) == vcs) {
                  filesToResolveUnderProject.add(file);
                }
              }
            }
          }
        }
      }, PerforceBundle.message("message.searching.for.files.to.resolve"), false, project);
      if (filesToResolveUnderProject.size() == 0) {
        Messages.showInfoMessage(PerforceBundle.message("message.text.no.files.to.resolve"),
                                 PerforceBundle.message("message.title.resolve"));
      }
      else {
        new PerforceMergeProvider(project).showMergeDialog(filesToResolveUnderProject);
      }
    }
    catch (VcsException e1) {
      Messages.showErrorDialog(e1.getLocalizedMessage(), PerforceBundle.message("message.title.resolve"));
    }
  }
}
