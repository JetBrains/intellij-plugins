package com.intellij.javascript.karma.execution;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.runners.RunContentBuilder;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.execution.ui.layout.PlaceInGrid;
import com.intellij.icons.AllIcons;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.server.KarmaServerLogComponent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithActions;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sergey Simonchik
 */
public class KarmaRunProgramRunner extends GenericProgramRunner {
  @NotNull
  @Override
  public String getRunnerId() {
    return "KarmaJsTestRunnerRun";
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
    if (executionResult == null) return null;
    KarmaServer karmaServer = null;
    if (state instanceof KarmaTestRunnerState) {
      karmaServer = ((KarmaTestRunnerState) state).getKarmaServer();
    }

    final MyRunContentBuilder contentBuilder = new MyRunContentBuilder(project, this, executor, executionResult, env);
    RunContentDescriptor descriptor = contentBuilder.showRunContent(contentToReuse);
    if (contentToReuse != null) {
      System.out.println("content to reuse");
    }
    if (karmaServer != null) {
      KarmaServerLogComponent logComponent = new KarmaServerLogComponent(project, descriptor, karmaServer);
      logComponent.installOn(contentBuilder.getUi());
    }
    return descriptor;
  }

  private static class MyRunContentBuilder extends RunContentBuilder {

    public MyRunContentBuilder(@NotNull Project project,
                               ProgramRunner runner,
                               Executor executor,
                               ExecutionResult executionResult,
                               @NotNull ExecutionEnvironment environment) {
      super(project, runner, executor, executionResult, environment);
    }

    @Override
    public RunnerLayoutUi getUi() {
      return super.getUi();
    }
  }
}
