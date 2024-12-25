package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

interface AdderRemover {
  @Nullable
  VcsException add(final @NotNull PerforceJob job, LocalChangeList list, Project project);
  @Nullable
  VcsException remove(final @NotNull PerforceJob job, LocalChangeList list, Project project);
}
