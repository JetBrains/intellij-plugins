package org.jetbrains.idea.perforce.operations;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collections;
import java.util.List;


public abstract class VcsOperationOnPath extends VcsOperation {
  protected String myPath;

  protected VcsOperationOnPath() {
  }

  protected VcsOperationOnPath(final String changeList, final String path) {
    super(changeList);
    myPath = path;
  }

  public String getPath() {
    return myPath;
  }

  @NotNull
  @Override
  String getInputPath() {
    return getPath();
  }

  @NotNull
  @Override
  String getOutputPath() {
    return getPath();
  }

  @SuppressWarnings("unused")
  public void setPath(final String path) {
    myPath = path;
  }

  protected FilePath getFilePath() {
    return VcsContextFactory.getInstance().createFilePathOn(new File(myPath), false);
  }

  @Override
  public List<String> getAffectedPaths() {
    return Collections.singletonList(myPath);
  }

  public static void markFileAsDirty(final Project project, @NonNls String path) {
    final FilePath filePath = VcsContextFactory.getInstance().createFilePathOn(new File(path));
    VcsDirtyScopeManager.getInstance(project).fileDirty(filePath);
  }
}