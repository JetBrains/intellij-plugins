package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.vcs.VcsException;
import org.jetbrains.annotations.Nullable;

public interface JobsTablePresentation {
  void refreshJobs(@Nullable PerforceJob job) throws VcsException;
  void addJob(final PerforceJob job);
  void removeSelectedJobs();
}
