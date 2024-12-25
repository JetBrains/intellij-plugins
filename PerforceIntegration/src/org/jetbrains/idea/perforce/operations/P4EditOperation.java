package org.jetbrains.idea.perforce.operations;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.perforce.FStat;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.PerforceCachingContentRevision;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;


public class P4EditOperation extends VcsOperationOnPath {
  private static final Logger LOG = Logger.getInstance(P4EditOperation.class);
  private static final @NonNls String CANNOT_FIND_ERROR = "the system cannot find";
  private static final @NonNls String CANNOT_FIND_ERROR_2 = "no such file or directory";
  private boolean mySuppressErrors;

  private boolean myNeedPathRefresh;

  @SuppressWarnings("unused") // used by deserialization reflection
  public P4EditOperation() {
    myNeedPathRefresh = true;
  }

  public P4EditOperation(String changeList, @NotNull VirtualFile file) {
    this(changeList, file, true);
  }

  public P4EditOperation(@NotNull String changeList, @NotNull VirtualFile file, boolean needPathRefresh) {
    super(changeList, file.getPath());
    myNeedPathRefresh = needPathRefresh;
  }

  public P4EditOperation(String changeList, final String path) {
    super(changeList, path);
  }

  @SuppressWarnings("unused") // used by deserialization reflection
  public boolean isSuppressErrors() {
    return mySuppressErrors;
  }

  public void setSuppressErrors(boolean suppressErrors) {
    mySuppressErrors = suppressErrors;
  }

  @Override
  public void execute(final Project project, ProcessingContext context) throws VcsException {
    final Ref<PerforceVcs> vcsRef = new Ref<>();
    final Ref<PerforceRunner> runnerRef = new Ref<>();
    ApplicationManager.getApplication().runReadAction(() -> {
      if (project.isDisposed()) return;
      vcsRef.set(PerforceVcs.getInstance(project));
      runnerRef.set(PerforceRunner.getInstance(project));
    });

    PerforceVcs vcs = vcsRef.get();
    PerforceRunner runner = runnerRef.get();
    if (vcs == null) return;

    try {
      final P4File p4File = P4File.createInefficientFromLocalPath(myPath);
      FStat p4FStat = p4File.getFstat(project, true);

      if (p4FStat == null) return;
      if ((p4FStat.status == FStat.STATUS_NOT_ADDED || p4FStat.status == FStat.STATUS_ONLY_LOCAL) &&
          p4FStat.local != FStat.LOCAL_BRANCHING) {
        throw new VcsException(
          PerforceBundle.message("confirmation.text.auto.edit.file.not.registered.on.server", p4File.getLocalPath()));
      }
      else if (p4FStat.status == FStat.STATUS_DELETED) {
        throw new VcsException(PerforceBundle.message("exception.text.file.deleted.from.server.cannot.edit", p4File.getLocalPath()));
      }
      else if (p4FStat.local == FStat.LOCAL_MOVE_DELETING) {
        return; // edit already done by rename/move handlers
      }
      else if (p4FStat.local != FStat.LOCAL_CHECKED_IN && p4FStat.local != FStat.LOCAL_INTEGRATING &&
               p4FStat.local != FStat.LOCAL_BRANCHING) {
        throw new VcsException(
          PerforceBundle.message("exception.text.file..should.not.be.readonly.cannot.edit", p4File.getLocalPath()));
      }

      long changeListNumber = getPerforceChangeList(project, p4File, context);
      runner.edit(p4File, changeListNumber);
    }
    catch (VcsException e) {
      if (mySuppressErrors) {
        // in allwrite workspace, 'p4 edit' is executed when modifying a file that's unchanged according to IDE status.
        // "not changed" file status might've been outdated, so the file might be not available for 'p4 edit'. Not a big deal.
        LOG.debug(e);
      } else {
        // check if file was deleted while we were waiting to perform background edit
        String message = StringUtil.toLowerCase(e.getMessage());
        if (!message.contains(CANNOT_FIND_ERROR) && !message.contains(CANNOT_FIND_ERROR_2)) {
          throw e;
        }
      }
    } finally {
      if (myNeedPathRefresh) {
        VirtualFile vFile = getFilePath().getVirtualFile();
        if (vFile != null) {
          vFile.refresh(true, false);
          vcs.asyncEditCompleted(vFile);
        }
      }
    }
    if (myNeedPathRefresh) {
      final FilePath filePath = getFilePath();
      VcsDirtyScopeManager.getInstance(project).fileDirty(filePath);
    }
  }

  @Override
  public Change getChange(final Project project, ChangeListManagerGate addGate) {
    FilePath path = getFilePath();
    final FileStatus status = addGate.getStatus(path);
    ContentRevision beforeRevision = FileStatus.ADDED.equals(status) ? null : PerforceCachingContentRevision.createOffline(project, path, path);
    ContentRevision afterRevision = CurrentContentRevision.create(path);
    return new Change(beforeRevision, afterRevision);
  }

}
