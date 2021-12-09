package org.jetbrains.idea.perforce.perforce.jobs;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.project.Project;

interface AdderRemover {
  @Nullable
  VcsException add(@NotNull final PerforceJob job, LocalChangeList list, Project project);
  @Nullable
  VcsException remove(@NotNull final PerforceJob job, LocalChangeList list, Project project);
}
