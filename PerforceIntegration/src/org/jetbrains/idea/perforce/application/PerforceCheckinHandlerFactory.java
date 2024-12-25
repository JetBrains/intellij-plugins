package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vcs.changes.ui.BooleanCommitOption;
import com.intellij.openapi.vcs.checkin.BeforeCheckinDialogHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.VcsCheckinHandlerFactory;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.actions.RevertAllUnchangedFilesAction;
import org.jetbrains.idea.perforce.actions.ShelfUtils;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.login.PerforceLoginManager;

import java.util.List;

import static org.jetbrains.idea.perforce.actions.ShelveAction.P4SHELF_EXECUTOR_ID;

public class PerforceCheckinHandlerFactory extends VcsCheckinHandlerFactory {
  public PerforceCheckinHandlerFactory() {
    super(PerforceVcs.getKey());
  }

  @Override
  protected @NotNull CheckinHandler createVcsHandler(@NotNull CheckinProjectPanel panel, @NotNull CommitContext commitContext) {
    final Project project = panel.getProject();
    return new CheckinHandler() {
      @Override
      public RefreshableOnComponent getBeforeCheckinConfigurationPanel() {
        return BooleanCommitOption.create(project, this, false,
                                          PerforceBundle.message("message.revert.unchanged.files"),
                                          () -> PerforceSettings.getSettings(project).REVERT_UNCHANGED_FILES_CHECKIN,
                                          value -> PerforceSettings.getSettings(project).REVERT_UNCHANGED_FILES_CHECKIN = value);
      }

      @Override
      public RefreshableOnComponent getAfterCheckinConfigurationPanel(Disposable parentDisposable) {
        if (panel.getCommitWorkflowHandler().getExecutor(P4SHELF_EXECUTOR_ID) == null) {
          return null;
        }

        return BooleanCommitOption.create(project, this, false,
                                          PerforceBundle.message("checkbox.revert.files.after.shelf"),
                                          () -> PerforceSettings.getSettings(project).REVERT_FILES_AFTER_SHELF,
                                          value -> PerforceSettings.getSettings(project).REVERT_FILES_AFTER_SHELF = value);
      }

      @Override
      public ReturnResult beforeCheckin() {
        if (PerforceSettings.getSettings(project).REVERT_UNCHANGED_FILES_CHECKIN) {
          RevertAllUnchangedFilesAction.revertUnchanged(project, panel.getVirtualFiles(), panel, null);
        }
        MultiMap<LocalChangeList, Change> map = MultiMap.create();
        for (Change change : panel.getSelectedChanges()) {
          LocalChangeList list = ChangeListManager.getInstance(project).getChangeList(change);
          if (list != null) {
            map.putValue(list, change);
          }
        }
        for (LocalChangeList list : map.keySet()) {
          List<ShelvedChange> shelvedChanges = PerforceManager.getInstance(project).getShelf().getShelvedChanges(list);
          if (!shelvedChanges.isEmpty()) {
            String message = PerforceBundle.message("changelist.has.shelved.changes", list.getName());
            int rc = Messages.showYesNoDialog(project, message, PerforceBundle.message("changelist.shelved.changes.found"), PerforceBundle.message("changelist.remove.shelved.changes"), Messages.getCancelButton(), Messages.getQuestionIcon());
            if (rc == Messages.NO) {
              return ReturnResult.CANCEL;
            }
            ShelfUtils.deleteFromShelf(shelvedChanges, project);
          }
        }

        return super.beforeCheckin();
      }
    };
  }

  @Override
  public BeforeCheckinDialogHandler createSystemReadyHandler(@NotNull Project project) {
    return new MyBeforeCheckinDialogHandler();
  }

  public static boolean beforeRemoteOperationCheck(Project project, final String operationName) {
    final PerforceSettings settings = PerforceSettings.getSettings(project);
    if (! settings.ENABLED) {
      final int result = Messages
        .showYesNoDialog(project, PerforceBundle.message("connection.offline.go.online", operationName),
                         PerforceBundle.message("connection.offline.title"),
                         Messages.getWarningIcon());
      if (Messages.YES == result) {
        settings.enable();
      }
    }
    boolean allOk = PerforceLoginManager.getInstance(project).checkAndRepairAll();
    return ProjectLevelVcsManager.getInstance(project).getAllActiveVcss().length > 1 || allOk && settings.ENABLED;
  }

  private static class MyBeforeCheckinDialogHandler extends BeforeCheckinDialogHandler {
    @Override
    public boolean beforeCommitDialogShown(@NotNull Project project, @NotNull List<? extends Change> changes, @NotNull Iterable<? extends CommitExecutor> executors, boolean showVcsCommit) {
      if (showVcsCommit) {
        return beforeRemoteOperationCheck(project, "Commit");
      }

      for (CommitExecutor executor : executors) {
        if (!(executor instanceof LocalCommitExecutor)) {
          return beforeRemoteOperationCheck(project, "Commit");
        }
      }
      return true;
    }
  }
}
