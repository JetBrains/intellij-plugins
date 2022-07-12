package org.jetbrains.idea.perforce.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.ConnectionKey;
import org.jetbrains.idea.perforce.application.PerforceNumberNameSynchronizer;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.jobs.EditChangelistJobsDialog;
import org.jetbrains.idea.perforce.perforce.jobs.JobDetailsLoader;
import org.jetbrains.idea.perforce.perforce.jobs.P4JobsLogicConn;
import org.jetbrains.idea.perforce.perforce.jobs.PerforceJob;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LinkChangeListToJobsAction extends AnAction {
  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    e.getPresentation().setText(PerforceBundle.messagePointer("action.link.changelist.to.jobs"));
    if (! enabled(e)) {
      e.getPresentation().setEnabledAndVisible(false);
      return;
    }
    e.getPresentation().setEnabledAndVisible(true);
  }

  private static boolean enabled(final AnActionEvent e) {
    final Project project = e.getProject();
    if (project == null || project.isDefault()) return false;
    if (! PerforceSettings.getSettings(project).ENABLED) return false;
    if (! PerforceSettings.getSettings(project).USE_PERFORCE_JOBS) return false;
    final ChangeList[] lists = e.getData(VcsDataKeys.CHANGE_LISTS);
    if (lists == null || (lists.length != 1)) {
      return false;
    }
    return !PerforceNumberNameSynchronizer.getInstance(project).getAllNumbers(lists[0].getName()).isEmpty();
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    if (! enabled(e)) {
      return;
    }
    final Project project = e.getProject();
    final ChangeList[] lists = e.getData(VcsDataKeys.CHANGE_LISTS);
    assert lists != null;
    final LocalChangeList list = (LocalChangeList) lists[0];

    final JobDetailsLoader loader = new JobDetailsLoader(project);
    final Map<ConnectionKey, P4JobsLogicConn> connMap = new HashMap<>();
    final Map<ConnectionKey, List<PerforceJob>> perforceJobs = new HashMap<>();
    loader.loadJobsForList(list, connMap, perforceJobs);
    if (perforceJobs.isEmpty()) {
      Messages.showInfoMessage(PerforceBundle.message("connection.no.valid.connections"), PerforceBundle.message("perforce"));
      return;
    }

    new EditChangelistJobsDialog(project, list, false, connMap, perforceJobs).show();
  }
}
