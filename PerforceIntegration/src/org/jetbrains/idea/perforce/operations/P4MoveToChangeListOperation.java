package org.jetbrains.idea.perforce.operations;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangesUtil;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.util.ProcessingContext;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;

import java.io.File;
import java.util.Map;


public class P4MoveToChangeListOperation extends VcsOperationOnPath {
  @SuppressWarnings("unused") // used by deserialization reflection
  public P4MoveToChangeListOperation() {
  }

  public P4MoveToChangeListOperation(Change c, String changeList) {
    super(changeList, ChangesUtil.getFilePath(c).getPath());
  }

  @Override
  public void execute(final Project project, ProcessingContext context) throws VcsException {
    File f = new File(myPath);
    long changeListNumber = getPerforceChangeList(project, P4File.createInefficientFromLocalPath(myPath), context);
    PerforceRunner.getInstance(project).reopen(new File[] { f }, changeListNumber);
    VcsDirtyScopeManager.getInstance(project).fileDirty(getFilePath());
  }

  @Override
  public void fillReopenedPaths(final Map<String, String> result) {
    result.put(myPath, myChangeList);
  }

  @Override
  public VcsOperation checkMerge(final VcsOperation oldOp) {
    if ((oldOp instanceof VcsOperationOnPath && ((VcsOperationOnPath) oldOp).getPath().equals(myPath)) ||
        (oldOp instanceof P4MoveRenameOperation && ((P4MoveRenameOperation)oldOp).newPath.equals(myPath))) {
      VcsOperation clone = (VcsOperation) oldOp.clone();
      clone.setChangeList(myChangeList);
      return clone;
    }
    return super.checkMerge(oldOp);
  }
}