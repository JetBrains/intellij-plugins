package org.jetbrains.idea.perforce.operations;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ProcessingContext;
import com.intellij.vcsUtil.ActionWithTempFile;
import org.jetbrains.idea.perforce.perforce.FStat;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;

import java.util.Arrays;
import java.util.List;


public class P4CopyOperation extends VcsOperationOnPath {
  private String mySourcePath;

  @SuppressWarnings("unused") // used by deserialization reflection
  public P4CopyOperation() {
  }

  public P4CopyOperation(String changeList, final VirtualFile vFile, final VirtualFile copyFrom) {
    super(changeList, vFile.getPath());
    mySourcePath = copyFrom.getPath();
  }

  @SuppressWarnings("unused")
  public String getSourcePath() {
    return mySourcePath;
  }

  @SuppressWarnings("unused")
  public void setSourcePath(final String sourcePath) {
    mySourcePath = sourcePath;
  }

  @Override
  public void execute(final Project project, ProcessingContext context) throws VcsException {
    final P4File sourceFile = P4File.createInefficientFromLocalPath(mySourcePath);
    final FStat sourceFStat = sourceFile.getFstat(project, false);
    if (sourceFStat.status != FStat.STATUS_NOT_ADDED &&
        sourceFStat.status != FStat.STATUS_ONLY_LOCAL &&
        !(sourceFStat.local == FStat.LOCAL_ADDING || sourceFStat.local == FStat.LOCAL_MOVE_ADDING)) {
      final P4File targetFile = P4File.createInefficientFromLocalPath(myPath);
      P4Connection srcConnection = PerforceConnectionManager.getInstance(project).getConnectionForFile(sourceFile);
      P4Connection targetConnection = PerforceConnectionManager.getInstance(project).getConnectionForFile(targetFile);
      if (P4MoveRenameOperation.isCompatibleConnection(srcConnection, targetConnection)) {
        new ActionWithTempFile(targetFile.getLocalFile()) {
          @Override
          protected void executeInternal() throws VcsException {
            final PerforceRunner runner = PerforceRunner.getInstance(project);
            runner.integrate(sourceFile, targetFile);
            runner.edit(targetFile);
          }
        }.execute();
        markFileAsDirty(project, myPath);
        return;
      }
    }
    new P4AddOperation(ChangeListManager.getInstance(project).getDefaultChangeList().getName(), myPath).execute(project, context);
  }

  @Override
  public List<String> getAffectedPaths() {
    return Arrays.asList(getPath(), mySourcePath);
  }
}
