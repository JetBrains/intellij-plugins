package org.jetbrains.idea.perforce.operations;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManagerGate;
import com.intellij.openapi.vcs.changes.CurrentContentRevision;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ProcessingContext;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.idea.perforce.application.PerforceChangeProvider;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.perforce.FStat;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;


public class P4AddOperation extends VcsOperationOnPath {
  @SuppressWarnings("unused") // used by deserialization reflection
  public P4AddOperation() {
  }

  public P4AddOperation(String changeList, String path) {
    super(changeList, path);
  }

  public P4AddOperation(String changeList, final VirtualFile file) {
    super(changeList, file.getPath());
  }

  @Override
  public void execute(final Project project, ProcessingContext context) throws VcsException {
    final P4File p4File = P4File.createInefficientFromLocalPath(myPath);

    // check whether it will be under any clientspec
    final FStat p4FStat = p4File.getFstat(project, true);
    if (p4FStat.status == FStat.STATUS_NOT_IN_CLIENTSPEC ||
        p4FStat.status == FStat.STATUS_UNKNOWN) {
      return;
    }
    // already being added or edited or something
    if (p4FStat.local == FStat.LOCAL_ADDING ||
        p4FStat.local == FStat.LOCAL_BRANCHING ||
        p4FStat.local == FStat.LOCAL_CHECKED_OUT ||
        p4FStat.local == FStat.LOCAL_INTEGRATING ||
        p4FStat.local == FStat.LOCAL_MOVE_ADDING) {
      return;
    }

    long changeListNumber = getPerforceChangeList(project, p4File, context);
    PerforceRunner runner = PerforceRunner.getInstance(project);
    if (p4FStat.local == FStat.Local.DELETING || p4FStat.local == FStat.Local.MOVE_DELETING) {
      runner.revert(p4File, true);
      runner.edit(p4File);
    } else {
      runner.add(p4File, changeListNumber);
    }

    VirtualFile file = getFilePath().getVirtualFile();
    if (file != null) {
      ((PerforceChangeProvider) PerforceVcs.getInstance(project).getChangeProvider()).clearUnversionedStatus(file);
    }

    p4File.clearCache();
    VcsUtil.markFileAsDirty(project, myPath);
  }

  @Override
  public void prepareOffline(Project project) {
    VcsUtil.markFileAsDirty(project, myPath);
  }

  @Override
  public Change getChange(final Project project, ChangeListManagerGate addGate) {
    return new Change(null, CurrentContentRevision.create(getFilePath()));
  }

  @Override
  public VcsOperation checkMerge(VcsOperation oldOp) {
    if (oldOp instanceof P4DeleteOperation && ((P4DeleteOperation)oldOp).getPath().equals(myPath)) {
      return new P4EditOperation(myChangeList, myPath);
    }

    return super.checkMerge(oldOp);
  }
}
