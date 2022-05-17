package org.jetbrains.idea.perforce.operations;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManagerGate;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.util.ProcessingContext;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.PerforceCachingContentRevision;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;

import java.io.File;


public class P4DeleteOperation extends VcsOperationOnPath {
  @SuppressWarnings("unused") // used by deserialization reflection
  public P4DeleteOperation() {
  }

  public P4DeleteOperation(String changeList, final FilePath item) {
    super(changeList, FileUtil.toSystemIndependentName(item.getPath()));
  }

  @Override
  public void execute(final Project project, ProcessingContext context) throws VcsException {
    final P4File p4File = P4File.createInefficientFromLocalPath(myPath);
    final long list = getPerforceChangeList(project, p4File, context);
    PerforceRunner.getInstance(project).assureDel(p4File, list);
    VcsUtil.markFileAsDirty(project, myPath);
  }

  @Override
  public Change getChange(final Project project, ChangeListManagerGate addGate) {
    FilePath path = VcsContextFactory.getInstance().createFilePathOn(new File(myPath));
    ContentRevision beforeRevision = PerforceCachingContentRevision.createOffline(project, path, path);
    return new Change(beforeRevision, null);
  }

  @Override
  @Nullable
  public VcsOperation checkMerge(final VcsOperation oldOp) {
    if (oldOp instanceof VcsOperationOnPath) {
      final boolean pathsEqual = FileUtil.pathsEqual(((VcsOperationOnPath)oldOp).getPath(), myPath);
      if (oldOp instanceof P4AddOperation && pathsEqual) {
        return null;
      }
      if (oldOp instanceof P4EditOperation && pathsEqual) {
        return this;
      }
    }
    return super.checkMerge(oldOp);
  }
}