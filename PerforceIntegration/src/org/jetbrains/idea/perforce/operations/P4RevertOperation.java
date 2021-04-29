package org.jetbrains.idea.perforce.operations;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangesUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class P4RevertOperation extends VcsOperation {
  public String beforePath;
  public String afterPath;

  @SuppressWarnings("unused") // used by deserialization reflection
  public P4RevertOperation() {
  }

  @NotNull
  @Override
  String getInputPath() {
    return StringUtil.notNullize(afterPath);
  }

  @NotNull
  @Override
  String getOutputPath() {
    return StringUtil.notNullize(beforePath);
  }

  public P4RevertOperation(Change c) {
    FilePath beforePath = ChangesUtil.getBeforePath(c);
    FilePath afterPath = ChangesUtil.getAfterPath(c);
    this.beforePath = (beforePath == null) ? null : beforePath.getPath();
    this.afterPath = (afterPath == null) ? null : afterPath.getPath();
  }

  @Override
  public void execute(final Project project, ProcessingContext context) throws VcsException {
    List<String> toRevert = new ArrayList<>();
    List<File> toDelete = new ArrayList<>();
    prepareRevert(toRevert, toDelete);
    for (String path : toRevert) {
      PerforceRunner.getInstance(project).revert(P4File.createInefficientFromLocalPath(path), true);
    }
    refreshAfterRevert(toRevert, toDelete, project);
  }

  boolean isRenameOrMove() {
    return beforePath != null && afterPath != null && !FileUtil.pathsEqual(beforePath, afterPath);
  }

  @Override
  public void fillReopenedPaths(final Map<String, String> result) {
    if (beforePath != null) {
      result.put(beforePath, null);
    }
    if (afterPath != null && (beforePath == null || !FileUtil.pathsEqual(beforePath, afterPath))) {
      result.put(afterPath, null);
    }
  }

  @Override
  @Nullable
  public VcsOperation checkMerge(final VcsOperation oldOp) {
    if (!isRenameOrMove()) {
      if (oldOp instanceof P4EditOperation || oldOp instanceof P4AddOperation) {
        VcsOperationOnPath opOnPath = (VcsOperationOnPath) oldOp;
        if (FileUtil.pathsEqual(opOnPath.getPath(), afterPath)) {
          return null;
        }
      }
    }
    else if (oldOp instanceof P4MoveRenameOperation) {
      P4MoveRenameOperation renameOp = (P4MoveRenameOperation) oldOp;
      if (FileUtil.pathsEqual(renameOp.newPath, afterPath)) {
        return null;
      }
    }
    return super.checkMerge(oldOp);
  }

  @Override
  public List<String> getAffectedPaths() {
    SmartList<String> result = new SmartList<>();
    ContainerUtil.addIfNotNull(result, beforePath);
    ContainerUtil.addIfNotNull(result, afterPath);
    return result;
  }

  void prepareRevert(List<String> toRevert, List<File> toDelete) {
    if (isRenameOrMove()) {
      toRevert.add(afterPath);
      toRevert.add(beforePath);
      toDelete.add(new File(afterPath));
    }
    else {
      String path = beforePath != null ? beforePath : afterPath;
      assert path != null;
      toRevert.add(path);
    }
  }

  static void refreshAfterRevert(List<String> toRevert, List<? extends File> toDelete, Project project) {
    RefreshForVcs refreshWorker = new RefreshForVcs();
    for (String file : toRevert) {
      refreshWorker.refreshFile(new File(file));
    }
    for (File file : toDelete) {
      FileUtil.delete(file);
      refreshWorker.addDeletedFile(file);
    }
    refreshWorker.run(project);
  }
}