package org.jetbrains.idea.perforce.actions;

import com.intellij.ide.SaveAndSyncHandler;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.committed.RefreshIncomingChangesAction;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.PerforceChangeList;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;

import java.util.Set;

/**
 * @author peter
 */
public class SyncToRevisionAction extends DumbAwareAction {

  public SyncToRevisionAction() {
    super(PerforceBundle.messagePointer("sync.to.revision"));
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getRequiredData(CommonDataKeys.PROJECT);
    String revision = String.valueOf(((PerforceChangeList)e.getRequiredData(VcsDataKeys.CHANGE_LISTS)[0]).getNumber());

    Set<VirtualFile> roots = PerforceConnectionManager.getInstance(project).getAllConnections().keySet();
    ProgressManager.getInstance().run(new Task.Backgroundable(project, PerforceBundle.message("project.updating"), true, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        try {
          for (VirtualFile root : roots) {
            PerforceRunner.getInstance(project).sync(P4File.create(root), revision);
          }
        }
        catch (VcsException error) {
          AbstractVcsHelper.getInstance(project).showError(error, PerforceBundle.message("sync.error"));
        }
      }

      @Override
      public void onSuccess() {
        RefreshIncomingChangesAction.doRefresh(project);
        SaveAndSyncHandler.getInstance().refreshOpenFiles();
        VfsUtil.markDirtyAndRefresh(true, true, true, roots.toArray(VirtualFile.EMPTY_ARRAY));
      }
    });
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    ChangeList[] lists = e.getData(VcsDataKeys.CHANGE_LISTS);
    e.getPresentation().setEnabledAndVisible(project != null && lists != null && lists.length == 1 && lists[0] instanceof PerforceChangeList);
  }
}
