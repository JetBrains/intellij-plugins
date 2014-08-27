package com.google.jstestdriver.idea.execution;

import com.google.jstestdriver.idea.server.JstdBrowserInfo;
import com.google.jstestdriver.idea.server.JstdServer;
import com.google.jstestdriver.idea.server.JstdServerLifeCycleAdapter;
import com.google.jstestdriver.idea.server.JstdServerRegistry;
import com.google.jstestdriver.idea.server.ui.JstdToolWindowManager;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.RunProfileStarter;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.AsyncGenericProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.execution.runners.RunContentBuilder;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.util.Alarm;
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
  protected AsyncResult<RunProfileStarter> prepare(@NotNull ExecutionEnvironment environment, @NotNull RunProfileState state) throws ExecutionException {
    JstdRunProfileState jstdState = JstdRunProfileState.cast(state);
    if (jstdState.getRunSettings().isExternalServerType()) {
      return AsyncResult.<RunProfileStarter>done(new JstdRunStarter(null, false));
    }
    JstdToolWindowManager jstdToolWindowManager = JstdToolWindowManager.getInstance(environment.getProject());
    jstdToolWindowManager.setAvailable(true);
    JstdServer server = JstdServerRegistry.getInstance().getServer();
    if (server != null && !server.isStopped()) {
      return AsyncResult.<RunProfileStarter>done(new JstdRunStarter(server, false));
    }
    final AsyncResult<RunProfileStarter> result = new AsyncResult<RunProfileStarter>();
    jstdToolWindowManager.restartServer(new NullableConsumer<JstdServer>() {
      @Override
      public void consume(@Nullable JstdServer server) {
        if (server != null) {
          result.setDone(new JstdRunStarter(server, false));
        }
        else {
          result.setDone(null);
        }
      }
    });
    return result;
  }

  public static class JstdRunStarter extends RunProfileStarter {
    private final JstdServer myServer;
    private final boolean myFromDebug;

    public JstdRunStarter(@Nullable JstdServer server, boolean fromDebug) {
      myServer = server;
      myFromDebug = fromDebug;
    }

    @Nullable
    @Override
    public RunContentDescriptor execute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment environment) throws ExecutionException {
      FileDocumentManager.getInstance().saveAllDocuments();
      JstdRunProfileState jstdState = JstdRunProfileState.cast(state);
      ExecutionResult executionResult = jstdState.executeWithServer(myServer);
      RunContentBuilder contentBuilder = new RunContentBuilder(executionResult, environment);
      final RunContentDescriptor descriptor = contentBuilder.showRunContent(environment.getContentToReuse());
      if (myServer != null && executionResult.getProcessHandler() instanceof NopProcessHandler) {
        myServer.addLifeCycleListener(new JstdServerLifeCycleAdapter() {
          @Override
          public void onBrowserCaptured(@NotNull JstdBrowserInfo info) {
            if (myFromDebug) {
              scheduleRestart(descriptor, 1000);
            }
            else {
              ExecutionUtil.restartIfActive(descriptor);
            }
            myServer.removeLifeCycleListener(this);
          }
        }, contentBuilder);
      }
      return descriptor;
    }

    private static void scheduleRestart(@NotNull final RunContentDescriptor descriptor, int timeoutMillis) {
      final Alarm alarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, descriptor);
      alarm.addRequest(new Runnable() {
        @Override
        public void run() {
          ExecutionUtil.restartIfActive(descriptor);
        }
      }, timeoutMillis);
    }
  }
}
