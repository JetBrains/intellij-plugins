package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.application.ConnectionKey;
import org.jetbrains.idea.perforce.application.PerforceNumberNameSynchronizer;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import java.util.*;

public class JobsWorker {
  private final Project myProject;
  private final PerforceRunner myRunner;
  private final PerforceNumberNameSynchronizer mySynchronizer;

  Project getProject() {
    return myProject;
  }

  public JobsWorker(final Project project) {
    myProject = project;

    mySynchronizer = PerforceNumberNameSynchronizer.getInstance(myProject);
    myRunner = PerforceRunner.getInstance(myProject);
  }

  public PerforceJobSpecification getSpec(final P4Connection connection) throws VcsException {
    final List<String> lines = myRunner.getJobSpecification(connection);
    final SpecificationParser parser = new SpecificationParser(lines);
    return parser.parse();
  }

  @NotNull
  public List<PerforceJob> getJobs(final PerforceJobSpecification specification, final JobsSearchSpecificator specificator,
                                   final P4Connection connection, final ConnectionKey key) throws VcsException {
    final List<String> lines = myRunner.getJobs(connection, specificator);
    final JobsOutputParser parser = new JobsOutputParser(specification, lines, connection, key);
    return parser.parse();
  }

  @Nullable
  private Long getNativeListNumber(LocalChangeList list, ConnectionKey key) {
    return mySynchronizer.getNumber(key, list.getName());
  }

  @NotNull
  public List<Pair<String, String>> loadJob(final PerforceJob job) throws VcsException {
    final List<String> lines = myRunner.getJobDetails(job);
    final JobDetailsParser parser = new JobDetailsParser(lines);
    return parser.parse();
  }

  public List<String> getJobNames(LocalChangeList list, P4Connection connection, ConnectionKey key) throws VcsException {
    Long number = getNativeListNumber(list, key);
    if (number == null) return Collections.emptyList();

    return new FixesOutputParser(myRunner.getJobsForChange(connection, number)).parseJobNames();
  }

  public static List<String> getFreeFields(final PerforceJobSpecification spec) {
    final List<String> result = new ArrayList<>();
    final Collection<PerforceJobField> fields = spec.getFields();
    for (PerforceJobField field : fields) {
      if (! StandardJobFields.isStandardField(field)) {
        result.add(field.getName());
      }
    }
    return result;
  }

  public void addJob(final PerforceJob job, LocalChangeList list) throws VcsException {
    Long number = getNativeListNumber(list, job.getConnectionKey());
    if (number != null) {
      myRunner.addJobForList(job.getConnection(), number, job.getName());
    }
  }

  public void removeJob(final PerforceJob job, LocalChangeList list) throws VcsException {
    Long number = getNativeListNumber(list, job.getConnectionKey());
    if (number != null) {
      myRunner.removeJobFromList(job.getConnection(), number, job.getName());
    }
  }

  public List<PerforceJob> getJobsForList(final PerforceJobSpecification specification, LocalChangeList list, P4Connection connection,
                                          ConnectionKey key) throws VcsException {
    final List<String> jobNames = getJobNames(list, connection, key);
    // list should be modifiable
    if (jobNames.isEmpty()) return new ArrayList<>();
    return getJobs(specification, new ByNamesConstraint(jobNames), connection, key);
  }
}
