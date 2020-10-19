package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.ConnectionKey;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JobDetailsLoader {
  private final Project myProject;
  private final JobsWorker myWorker;

  public JobDetailsLoader(final Project project) {
    myWorker = new JobsWorker(project);
    myProject = project;
  }

  public List<Pair<String, String>> load(final PerforceJob job) throws VcsException {
    final List<Pair<String, String>> result = new ArrayList<>();
    final Ref<VcsException> exception = new Ref<>();
    final Runnable loader = () -> {
      try {
        result.addAll(myWorker.loadJob(job));
      }
      catch (VcsException e) {
        exception.set(e);
      }
    };
    if (! ApplicationManager.getApplication().isDispatchThread()) {
      loader.run();
    } else {
      ProgressManager.getInstance().runProcessWithProgressSynchronously(loader, PerforceBundle.message("job.loading.fields"), true, myProject);
    }
    if (! exception.isNull()) {
      throw exception.get();
    }
    return result;
  }

  public void fillConnections(final LocalChangeList list, final Map<ConnectionKey, P4JobsLogicConn> connMap) {
    ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
      final ErrorReporter reporter = new ErrorReporter(PerforceBundle.message("job.getting.related"));

      final Map<ConnectionKey, P4Connection> map = ConnectionSelector.getConnections(myProject, list);
      if (map.isEmpty()) return;

      for (Map.Entry<ConnectionKey, P4Connection> entry : map.entrySet()) {
        final ConnectionKey key = entry.getKey();
        final P4Connection connection = entry.getValue();
        try {
          final P4JobsLogicConn p4JobsLogicConn = connMap.get(key);
          final PerforceJobSpecification spec = (p4JobsLogicConn == null) ? myWorker.getSpec(connection) : p4JobsLogicConn.getSpec();
          connMap.put(key, new P4JobsLogicConn(connection, spec, null));
        }
        catch (VcsException e1) {
          reporter.report(myProject, e1);
        }
      }
    }, PerforceBundle.message("job.getting.specs"), false, myProject);
  }

  public void loadJobsForList(final LocalChangeList list, final Map<ConnectionKey, P4JobsLogicConn> connMap,
                              final Map<ConnectionKey, List<PerforceJob>> perforceJobs) {
    final Runnable loader = () -> {
      final ErrorReporter reporter = new ErrorReporter(PerforceBundle.message("job.getting.related"));
      final Map<ConnectionKey, P4Connection> map = ConnectionSelector.getConnections(myProject, list);
      if (map.isEmpty()) return;

      for (Map.Entry<ConnectionKey, P4Connection> entry : map.entrySet()) {
        final ConnectionKey key = entry.getKey();
        final P4Connection connection = entry.getValue();
        try {
          final List<String> jobNames = myWorker.getJobNames(list, connection, key);
          final P4JobsLogicConn p4JobsLogicConn = connMap.get(key);
          final PerforceJobSpecification spec = (p4JobsLogicConn == null) ? myWorker.getSpec(connection) : p4JobsLogicConn.getSpec();
          connMap.put(key, new P4JobsLogicConn(connection, spec, null));
          perforceJobs.put(key, jobNames.isEmpty()
                                ? Collections.emptyList()
                                : myWorker.getJobs(spec, new ByNamesConstraint(jobNames), connection, key));
        }
        catch (VcsException e1) {
          reporter.report(myProject, e1);
        }
      }
    };
    if (ApplicationManager.getApplication().isDispatchThread()) {
      ProgressManager.getInstance().runProcessWithProgressSynchronously(loader, PerforceBundle.message("job.loading.jobs.for.changelist"), false, myProject);
    } else {
      loader.run();
    }
  }

}
