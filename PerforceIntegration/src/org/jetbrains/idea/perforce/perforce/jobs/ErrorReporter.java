package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.util.WaitForProgressToShow;
import org.jetbrains.idea.perforce.PerforceBundle;

public class ErrorReporter {
  private static final Logger LOG = Logger.getInstance(ErrorReporter.class);

  private final String myOperation;

  public ErrorReporter(String operation) {
    myOperation = operation;
  }

  public void report(final Project project, final VcsException e) {
    LOG.info(e);
    final String message = PerforceBundle.message("error.during", myOperation, e.getMessage());
    WaitForProgressToShow.runOrInvokeLaterAboveProgress(() -> Messages.showErrorDialog(project, message, PerforceBundle.message("job.jobs.error")), null, project);
  }
}
