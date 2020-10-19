package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import org.jetbrains.annotations.NotNull;

public class WiseAdderRemover implements AdderRemover {
  private final AdderRemover myMemory;
  private final AdderRemover myWriting;

  public WiseAdderRemover(final Project project, final JobsTablePresentation presentation) {
    myMemory = new MemoryAdderRemover(presentation);
    myWriting = new WritingAdderRemover(new JobsWorker(project), presentation);
  }

  @Override
  public VcsException add(@NotNull PerforceJob job, LocalChangeList list, Project project) {
    if (list.hasDefaultName()) {
      return myMemory.add(job, list, project);
    }
    return myWriting.add(job, list, project);
  }

  @Override
  public VcsException remove(@NotNull PerforceJob job, LocalChangeList list, Project project) {
    if (list.hasDefaultName()) {
      return myMemory.remove(job, list, project);
    }
    return myWriting.remove(job, list, project);
  }
}
