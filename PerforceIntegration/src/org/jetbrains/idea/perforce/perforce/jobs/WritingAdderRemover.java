package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.PerforceBundle;

class WritingAdderRemover implements AdderRemover {
  private final JobsTablePresentation myPresentation;
  private final JobsWorker myWorker;

  WritingAdderRemover(final JobsWorker worker, JobsTablePresentation presentation) {
    myWorker = worker;
    myPresentation = presentation;
  }

  @Override
  @Nullable
  public VcsException add(@NotNull final PerforceJob job, final LocalChangeList list, final Project project) {
    final Ref<VcsException> exceptionRef = new Ref<>();
    ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
      try {
        myWorker.addJob(job, list);
        myPresentation.refreshJobs(job);
      }
      catch (VcsException e) {
        exceptionRef.set(e);
      }
    }, PerforceBundle.message("job.adding.to.changelist.progress"), false, myWorker.getProject());
    return exceptionRef.get();
  }

  @Override
  @Nullable
  public VcsException remove(@NotNull final PerforceJob job, final LocalChangeList list, final Project project) {
    final Ref<VcsException> exceptionRef = new Ref<>();
    ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
      try {
        myWorker.removeJob(job, list);
        myPresentation.refreshJobs(null);
      }
      catch (VcsException e) {
        exceptionRef.set(e);
      }
    }, PerforceBundle.message("job.removing.from.changelist.progress"), false, myWorker.getProject());
    return exceptionRef.get();
  }
}
