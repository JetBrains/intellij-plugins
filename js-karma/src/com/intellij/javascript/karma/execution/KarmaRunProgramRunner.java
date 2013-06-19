package com.intellij.javascript.karma.execution;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.runners.RunContentBuilder;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sergey Simonchik
 */
public class KarmaRunProgramRunner extends GenericProgramRunner {
  private static final Logger LOG = Logger.getInstance(KarmaRunProgramRunner.class);

  @NotNull
  @Override
  public String getRunnerId() {
    return "KarmaJavaScriptTestRunnerRun";
  }

  @Override
  public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
    return DefaultRunExecutor.EXECUTOR_ID.equals(executorId) && profile instanceof KarmaRunConfiguration;
  }

  @Nullable
  @Override
  protected RunContentDescriptor doExecute(Project project,
                                           Executor executor,
                                           RunProfileState state,
                                           RunContentDescriptor contentToReuse,
                                           ExecutionEnvironment env) throws ExecutionException {
    FileDocumentManager.getInstance().saveAllDocuments();
    ExecutionResult executionResult = state.execute(executor, this);
    if (executionResult == null) {
      return null;
    }
    KarmaConsoleView consoleView = KarmaConsoleView.get(executionResult);
    if (consoleView == null) {
      LOG.error("Can't get KarmaConsoleView from executionResult!");
      return null;
    }
    RunContentBuilder contentBuilder = new RunContentBuilder(project, this, executor, executionResult, env);
    final RunContentDescriptor descriptor = contentBuilder.showRunContent(contentToReuse);

    final KarmaServer karmaServer = consoleView.getKarmaExecutionSession().getKarmaServer();
    if (!karmaServer.isReady() || !karmaServer.hasCapturedBrowsers()) {
      karmaServer.doWhenReadyWithCapturedBrowser(new Runnable() {
        @Override
        public void run() {
          descriptor.getRestarter().run();
        }
      });
    }
    return descriptor;
  }

}
