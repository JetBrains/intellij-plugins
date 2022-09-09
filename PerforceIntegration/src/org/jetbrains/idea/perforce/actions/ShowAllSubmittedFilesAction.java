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

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.vcsUtil.VcsRunnable;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.ChangeListData;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.PerforceFileRevision;
import org.jetbrains.idea.perforce.application.PerforceVcsRevisionNumber;
import org.jetbrains.idea.perforce.perforce.PerforceChangeCache;
import org.jetbrains.idea.perforce.perforce.PerforceChangeList;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import java.util.Date;

public class ShowAllSubmittedFilesAction extends AnAction implements DumbAware {
  public ShowAllSubmittedFilesAction() {
    super(PerforceBundle.messagePointer("action.text.show.all.submitted"), AllIcons.Actions.ListChanges);
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    final Project project = e.getData(CommonDataKeys.PROJECT);
    if (project == null) {
      e.getPresentation().setEnabled(false);
      return;
    }
    e.getPresentation().setEnabled(e.getData(VcsDataKeys.VCS_FILE_REVISION) != null);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    final Project project = e.getData(CommonDataKeys.PROJECT);
    if (project == null) return;
    final VcsFileRevision revision = e.getData(VcsDataKeys.VCS_FILE_REVISION);
    if (revision != null) {
      final PerforceFileRevision perfRevision = ((PerforceFileRevision)revision);

      showAllSubmittedFiles(project, ((PerforceVcsRevisionNumber)perfRevision.getRevisionNumber()).getChangeNumber(),
                            perfRevision.getCommitMessage(),
                            perfRevision.getRevisionDate(),
                            perfRevision.getAuthor(),
                            perfRevision.getConnection());

    }
  }

  public static void showAllSubmittedFiles(final Project project,
                                           final long number,
                                           final String submitMessage,
                                           final Date date,
                                           final String user,
                                           @NotNull final P4Connection connection) {

    try {
      final PerforceChangeList changeList = getSubmittedChangeList(project, number, submitMessage, date, user, connection);
      if (changeList != null) {
        if (changeList.getChanges().size() > 300) {
          Messages.showInfoMessage(PerforceBundle.message("show.all.files.from.change.list.too.many.files.affected.error.message"),
                                   getTitle(number));
        }
        else {
          AbstractVcsHelper.getInstance(project).showChangesListBrowser(changeList, getTitle(number));
        }
      }
    }
    catch(VcsException ex) {
      Messages
        .showErrorDialog(PerforceBundle.message("message.text.cannot.show.revisions", ex.getLocalizedMessage()), getTitle(number));
    }
  }

  @Nullable
  public static PerforceChangeList getSubmittedChangeList(Project project,
                                                           long number,
                                                           String submitMessage,
                                                           Date date,
                                                           String user,
                                                           P4Connection connection) throws VcsException {
    final ChangeListData data = new ChangeListData();
    data.NUMBER = number;
    data.USER = user;
    data.DATE  = ChangeListData.DATE_FORMAT.format(date);
    data.DESCRIPTION = submitMessage;

    final PerforceChangeList changeList = new PerforceChangeList(data, project, connection, new PerforceChangeCache(project));

    boolean result = VcsUtil.runVcsProcessWithProgress(new VcsRunnable() {
      @Override
      public void run() throws VcsException {
        changeList.getChanges();
      }
    }, PerforceBundle.message("show.all.files.from.change.list.searching.for.changed.files.progress.title"), true, project);

    return result ? changeList : null;
  }

  private static String getTitle(long changeListNumber) {
    return PerforceBundle.message("dialog.title.show.all.revisions.in.changelist", changeListNumber);
  }
}
