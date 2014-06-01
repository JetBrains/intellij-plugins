package com.google.jstestdriver.idea.execution;

import com.google.jstestdriver.idea.server.JstdBrowserInfo;
import com.google.jstestdriver.idea.server.JstdServer;
import com.google.jstestdriver.idea.server.JstdServerLifeCycleAdapter;
import com.google.jstestdriver.idea.server.JstdServerRegistry;
import com.google.jstestdriver.idea.server.ui.JstdToolWindowManager;
import com.google.jstestdriver.idea.util.JstdUtil;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.RunProfileStarter;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.AsyncGenericProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.RunContentBuilder;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.util.NullableConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sergey Simonchik
 */
public class JstdRunProgramRunner extends AsyncGenericProgramRunner {

  @NotNull
  @Override
  public String getRunnerId() {
    return "JsTestDriverClientRunner";
  }

  @Override
  public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
    return DefaultRunExecutor.EXECUTOR_ID.equals(executorId) && profile instanceof JstdRunConfiguration;
  }

  @NotNull
  @Override
  protected AsyncResult<RunProfileStarter> prepare(@NotNull Project project,
                                                   @NotNull ExecutionEnvironment environment,
                                                   @NotNull RunProfileState state) throws ExecutionException {
    if (state instanceof JstdRunProfileState) {
      JstdRunProfileState jstdState = (JstdRunProfileState) state;
      if (jstdState.getRunSettings().isExternalServerType()) {
        return AsyncResult.<RunProfileStarter>done(new MyStarter(null, this));
      }
    }
    JstdToolWindowManager jstdToolWindowManager = JstdToolWindowManager.getInstance(project);
    jstdToolWindowManager.setAvailable(true);
    JstdServerRegistry registry = JstdServerRegistry.getInstance();
    JstdServer server = registry.getServer();
    if (server != null && server.isProcessRunning() && !server.isStopped()) {
      return AsyncResult.<RunProfileStarter>done(new MyStarter(server, this));
    }
    final AsyncResult<RunProfileStarter> result = new AsyncResult<RunProfileStarter>();
    jstdToolWindowManager.restartServer(new NullableConsumer<JstdServer>() {
      @Override
      public void consume(@Nullable JstdServer server) {
        if (server != null) {
          result.setDone(new MyStarter(server, JstdRunProgramRunner.this));
        }
        else {
          result.setDone(null);
        }
      }
    });
    return result;
  }

  private static class MyStarter extends RunProfileStarter {

    private final JstdServer myServer;
    private final JstdRunProgramRunner myRunner;

    private MyStarter(@Nullable JstdServer server, @NotNull JstdRunProgramRunner runner) {
      myServer = server;
      myRunner = runner;
    }

    @Nullable
    @Override
    public RunContentDescriptor execute(@NotNull Project project,
                                        @NotNull Executor executor,
                                        @NotNull RunProfileState state,
                                        @Nullable RunContentDescriptor contentToReuse,
                                        @NotNull ExecutionEnvironment environment) throws ExecutionException {
      FileDocumentManager.getInstance().saveAllDocuments();
      JstdRunProfileState jstdState = (JstdRunProfileState) state;
      ExecutionResult executionResult = jstdState.executeWithServer(myServer);
      RunContentBuilder contentBuilder = new RunContentBuilder(myRunner, executionResult, environment);
      final RunContentDescriptor descriptor = contentBuilder.showRunContent(contentToReuse);
      if (myServer != null && executionResult.getProcessHandler() instanceof NopProcessHandler) {
        myServer.addLifeCycleListener(new JstdServerLifeCycleAdapter() {
          @Override
          public void onBrowserCaptured(@NotNull JstdBrowserInfo info) {
            JstdUtil.restart(descriptor);
            myServer.removeLifeCycleListener(this);
          }
        }, contentBuilder);
      }
      return descriptor;
    }
  }
}
