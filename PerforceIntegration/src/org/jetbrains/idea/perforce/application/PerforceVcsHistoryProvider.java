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
package org.jetbrains.idea.perforce.application;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ChangesUtil;
import com.intellij.openapi.vcs.history.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.ColumnInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.actions.ShowAllSubmittedFilesAction;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.P4Revision;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PerforceVcsHistoryProvider implements VcsHistoryProvider {
  private final PerforceVcs myVcs;
  private static final ColumnInfo<VcsFileRevision, String> REVISION = new ColumnInfo<>(
    PerforceBundle.message("file.history.revision.column.name")) {
    @Override
    public String valueOf(VcsFileRevision vcsFileRevision) {
      if (!(vcsFileRevision instanceof PerforceFileRevision)) return "";
      return String.valueOf(((PerforceFileRevision)vcsFileRevision).getVersionNumber());
    }

    @Override
    public Comparator<VcsFileRevision> getComparator() {
      return (r1, r2) -> {
        if (!(r1 instanceof PerforceFileRevision)) return 1;
        if (!(r2 instanceof PerforceFileRevision)) return -1;
        return (int)(((PerforceFileRevision)r1).getVersionNumber() - ((PerforceFileRevision)r2).getVersionNumber());
      };
    }
  };

  private static final ColumnInfo<VcsFileRevision, String> ACTION = new ColumnInfo<>(
    PerforceBundle.message("file.history.action.column.name")) {
    @Override
    public String valueOf(VcsFileRevision vcsFileRevision) {
      if (!(vcsFileRevision instanceof PerforceFileRevision)) return "";
      return ((PerforceFileRevision)vcsFileRevision).getAction();
    }
  };

  private static final ColumnInfo<VcsFileRevision, String> CLIENT = new ColumnInfo<>(
    PerforceBundle.message("file.history.client.column.name")) {
    @Override
    public String valueOf(VcsFileRevision vcsFileRevision) {
      if (!(vcsFileRevision instanceof PerforceFileRevision)) return "";
      return ((PerforceFileRevision)vcsFileRevision).getClient();
    }
  };
  private final PerforceRunner myRunner;


  public PerforceVcsHistoryProvider(PerforceVcs vcs) {
    myVcs = vcs;
    myRunner = PerforceRunner.getInstance(vcs.getProject());
  }

  @Override
  public VcsDependentHistoryComponents getUICustomization(final VcsHistorySession session, JComponent forShortcutRegistration) {
    return VcsDependentHistoryComponents.createOnlyColumns(new ColumnInfo[]{
      REVISION, ACTION, CLIENT
    });


  }

  @Override
  public AnAction[] getAdditionalActions(final Runnable refresher) {
    return new AnAction[]{new ShowBranchesAction(refresher), new ShowAllSubmittedFilesAction()};
  }

  @Override
  public boolean isDateOmittable() {
    return false;
  }

  @Override
  @Nullable
  public String getHelpId() {
    return null;
  }

  @Override
  public VcsHistorySession createSessionFor(FilePath filePath) throws VcsException {
    filePath = ChangesUtil.getCommittedPath(myVcs.getProject(), filePath);

    final P4Connection connection = myVcs.getSettings().getConnectionForFile(filePath.getIOFile());
    if (connection == null) {
      throw new VcsException(PerforceBundle.message("error.invalid.perforce.settings"));
    }
    final P4File p4File = P4File.create(filePath);
    p4File.invalidateFstat();
    final List<VcsFileRevision> revisions = new ArrayList<>();
    PerforceRunner runner = PerforceRunner.getInstance(myVcs.getProject());
    for (P4Revision p4Revision : runner.filelog(p4File, PerforceSettings.getSettings(myVcs.getProject()).SHOW_BRANCHES_HISTORY)) {
      revisions.add(new PerforceFileRevision(p4Revision, connection, myVcs.getProject()));
    }
    return createSession(p4File, revisions, myRunner.getCurrentRevision(p4File));
  }

  private VcsAbstractHistorySession createSession(final P4File p4File,
                                                  final List<VcsFileRevision> revisions,
                                                  final VcsRevisionNumber currentRevisionNumber) {
    return new VcsAbstractHistorySession(revisions, currentRevisionNumber) {
      @Override
      @Nullable
      public VcsRevisionNumber calcCurrentRevisionNumber() {
        return myRunner.getCurrentRevision(p4File);
      }

      @Override
      public boolean isCurrentRevision(VcsRevisionNumber rev) {
        if (!(rev instanceof PerforceVcsRevisionNumber)) return false;
        PerforceVcsRevisionNumber p4rev = (PerforceVcsRevisionNumber) rev;
        PerforceVcsRevisionNumber currentRev = (PerforceVcsRevisionNumber) getCachedRevision();
        if (currentRev != null && p4rev.getRevisionNumber() == currentRev.getRevisionNumber() && !p4rev.isBranched()) {
          final String curPath = currentRev.getDepotPath();
          final String revPath = p4rev.getDepotPath();
          return curPath == null || revPath == null || curPath.equals(revPath);
        }
        return false;
      }

      @Override
      public boolean shouldBeRefreshed() {
        final VcsRevisionNumber oldValue = getCachedRevision();
        final VcsRevisionNumber newNumber = calcCurrentRevisionNumber();
        // if perforce error, it's likely due to connection(?) problem, no sense in refreshing -> as if not changed
        if (newNumber == null) return false;
        setCachedRevision(newNumber);
        return oldValue == null || (((PerforceVcsRevisionNumber) oldValue).getRevisionNumber() !=
                                    ((PerforceVcsRevisionNumber) newNumber).getRevisionNumber());
      }

      @Override
      public VcsHistorySession copy() {
        return createSession(p4File, getRevisionList(), getCurrentRevisionNumber());
      }
    };
  }

  @Override
  public void reportAppendableHistory(FilePath path, VcsAppendableHistorySessionPartner partner) throws VcsException {
    // no need to make faster=)
    final VcsHistorySession session = createSessionFor(path);
    partner.reportCreatedEmptySession((VcsAbstractHistorySession) session);
  }

  @Override
  public boolean supportsHistoryForDirectories() {
    return true;
  }

  @Override
  public DiffFromHistoryHandler getHistoryDiffHandler() {
    return null;
  }

  @Override
  public boolean canShowHistoryFor(@NotNull VirtualFile file) {
    return true;
  }

  class ShowBranchesAction extends ToggleAction implements DumbAware {
    private final Runnable myPanelRefresher;

    ShowBranchesAction(final Runnable panelRefresher) {
      super(PerforceBundle.message("action.name.show.branches"), null, AllIcons.Vcs.Branch);
      myPanelRefresher = panelRefresher;
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
      return PerforceSettings.getSettings(myVcs.getProject()).SHOW_BRANCHES_HISTORY;
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
      PerforceSettings.getSettings(myVcs.getProject()).SHOW_BRANCHES_HISTORY= state;
      if (myPanelRefresher != null) {
        myPanelRefresher.run();
      }
    }
  }
}
