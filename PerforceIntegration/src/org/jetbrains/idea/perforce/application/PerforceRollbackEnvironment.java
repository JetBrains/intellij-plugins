package org.jetbrains.idea.perforce.application;

import com.intellij.CommonBundle;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vcs.rollback.RollbackEnvironment;
import com.intellij.openapi.vcs.rollback.RollbackProgressListener;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.operations.P4RevertOperation;
import org.jetbrains.idea.perforce.operations.VcsOperation;
import org.jetbrains.idea.perforce.operations.VcsOperationLog;
import org.jetbrains.idea.perforce.perforce.FStat;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;

import java.util.*;


public class PerforceRollbackEnvironment implements RollbackEnvironment {
  private static final Logger LOG = Logger.getInstance(PerforceRollbackEnvironment.class);

  private final Project myProject;
  private final PerforceRunner myRunner;

  public PerforceRollbackEnvironment(final Project project) {
    myProject = project;
    myRunner = PerforceRunner.getInstance(project);
  }

  @Override
  @Nls(capitalization = Nls.Capitalization.Title)
  @NotNull
  public String getRollbackOperationName() {
    return PerforceBundle.message("operation.name.revert");
  }

  @Override
  public void rollbackChanges(List<? extends Change> changes, final List<VcsException> vcsExceptions, @NotNull final RollbackProgressListener listener) {
    Map<Long, P4Connection> lists2Delete = getChangeListsToDelete(changes, vcsExceptions);

    ArrayList<VcsOperation> operations = new ArrayList<>();
    for (Change change : changes) {
      operations.add(new P4RevertOperation(change));
    }
    if (!VcsOperationLog.getInstance(myProject).runOperations(operations, CommonBundle.message("button.revert"), PerformInBackgroundOption.ALWAYS_BACKGROUND, vcsExceptions)) {
      return;
    }

    boolean listsChanged = false;
    for (Map.Entry<Long, P4Connection> entry : lists2Delete.entrySet()) {
      long id = entry.getKey();
      try {
        if (myRunner.deleteChangeList(entry.getValue(), id, true, true, true)) {
          listsChanged = true;
        }
      }
      catch (VcsException e) {
        vcsExceptions.add(e);
      }
    }

    if (listsChanged) {
      VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    }
  }

  private Map<Long, P4Connection> getChangeListsToDelete(List<? extends Change> changes, List<? super VcsException> vcsExceptions) {
    Map<Long, P4Connection> lists2Delete = new LinkedHashMap<>();
    for (LocalChangeList list : ChangeListManager.getInstance(myProject).getChangeListsCopy()) {
      Collection<Change> reverted = list.getChanges();
      if (!reverted.isEmpty() && changes.containsAll(reverted)) {
        for (Change c : reverted) {
          FilePath afterPath = ChangesUtil.getAfterPath(c);
          if (afterPath != null) {
            P4Connection connection = PerforceConnectionManager.getInstance(myProject).getConnectionForFile(afterPath.getIOFile());
            if (connection != null) {
              try {
                PerforceManager.ensureValidClient(myProject, connection);
              }
              catch (VcsException e) {
                vcsExceptions.add(e);
              }

              ConnectionKey key = connection.getConnectionKey();
              Long number = PerforceNumberNameSynchronizer.getInstance(myProject).getNumber(key, list.getName());
              if (number != null) {
                lists2Delete.put(number, connection);
              }
              break;
            }
          }
        }
      }
    }
    return lists2Delete;
  }

  @Override
  public void rollbackMissingFileDeletion(List<? extends FilePath> files, final List<? super VcsException> exceptions,
                                          final RollbackProgressListener listener) {
    for (FilePath file : files) {
      listener.accept(file);
      try {
        P4File p4file = P4File.create(file);
        FStat fStat;
        try {
          fStat = p4file.getFstat(myProject, true);
        }
        catch (VcsException e) {
          LOG.info(e);
          continue;
        }
        if (fStat.local == FStat.LOCAL_CHECKED_OUT || fStat.local == FStat.LOCAL_INTEGRATING || fStat.local == FStat.LOCAL_ADDING) {
          myRunner.revert(p4file, false);
        }
        else {
          myRunner.sync(p4file, true);
        }
        VcsDirtyScopeManager.getInstance(myProject).fileDirty(file);
      }
      catch (VcsException e) {
        exceptions.add(e);
      }
    }
  }

  @Override
  public void rollbackModifiedWithoutCheckout(final List<? extends VirtualFile> files, final List<? super VcsException> exceptions,
                                              final RollbackProgressListener listener) {
    MultiMap<P4Connection, VirtualFile> map = FileGrouper.distributeFilesByConnection(files, myProject);
    for (Map.Entry<P4Connection, Collection<VirtualFile>> entry : map.entrySet()) {
      List<String> paths = new ArrayList<>();
      List<P4File> p4Files = new ArrayList<>();
      for(VirtualFile file: entry.getValue()) {
        P4File p4File = P4File.create(file);
        p4Files.add(p4File);
        paths.add(p4File.getEscapedPath());
      }

      P4Connection connection = entry.getKey();
      try {
        myRunner.editAll(p4Files, -1, false, connection);
        myRunner.revertAll(paths, connection);
      }
      catch (VcsException e) {
        exceptions.add(e);
      }
    }

  }

  @Override
  public void rollbackIfUnchanged(final VirtualFile file) {
    /*Task.Backgroundable rollbackTask =
      new Task.Backgroundable(myProject, PerforceBundle.message("progress.title.reverting.unmodified.file")) {
        public void run(@NotNull final ProgressIndicator indicator) {
          try {
            final boolean reverted = PerforceRunner.getInstance(myProject).revertUnchanged(P4File.create(file));
            if (reverted) {
              file.refresh(true, false, new Runnable() {
                public void run() {
                  VcsDirtyScopeManager.getInstance(myProject).fileDirty(file);
                }
              });
            }
          }
          catch (VcsException e) {
            // ignore
            LOG.debug(e);
          }
        }
      };
    try {
      PerforceVcs.getInstance(myProject).runTask(rollbackTask, Collections.singletonList(file));
    }
    catch (VcsException e) {
      LOG.debug(e);
    }*/
  }

  @Override
  public Collection<? extends AnAction> createCustomRollbackActions() {
    return Collections.singleton(ActionManager.getInstance().getAction("RevertUnchanged"));
  }
}
