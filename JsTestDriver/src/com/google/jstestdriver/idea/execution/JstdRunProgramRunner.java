// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.google.jstestdriver.idea.execution;

import com.google.jstestdriver.idea.server.JstdBrowserInfo;
import com.google.jstestdriver.idea.server.JstdServer;
import com.google.jstestdriver.idea.server.JstdServerLifeCycleAdapter;
import com.google.jstestdriver.idea.server.JstdServerRegistry;
import com.google.jstestdriver.idea.server.ui.JstdToolWindowManager;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.process.NopProcessHandler;
import com.intellij.execution.runners.AsyncProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.execution.runners.RunContentBuilder;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.util.Alarm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.Promise;
import org.jetbrains.concurrency.Promises;

/**
 * @author Sergey Simonchik
 */
public class JstdRunProgramRunner extends AsyncProgramRunner {
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
  protected Promise<RunContentDescriptor> execute(@NotNull ExecutionEnvironment environment, @NotNull RunProfileState state) throws ExecutionException {
    JstdRunProfileState jstdState = JstdRunProfileState.cast(state);
    if (jstdState.getRunSettings().isExternalServerType()) {
      return Promises.resolvedPromise(start(null, false, state, environment));
    }
    JstdToolWindowManager jstdToolWindowManager = JstdToolWindowManager.getInstance(environment.getProject());
    jstdToolWindowManager.setAvailable(true);
    JstdServer server = JstdServerRegistry.getInstance().getServer();
    if (server != null && !server.isStopped()) {
      return Promises.resolvedPromise(start(server, false, state, environment));
    }
    return jstdToolWindowManager.restartServer()
      .thenAsync(it -> {
        try {
          return it == null ? null : Promises.resolvedPromise(start(it, false, state, environment));
        }
        catch (ExecutionException e) {
          return Promises.rejectedPromise(e);
        }
      });
  }

  public static RunContentDescriptor start(@Nullable JstdServer server,
                                           boolean fromDebug,
                                           @NotNull RunProfileState state,
                                           @NotNull ExecutionEnvironment environment) throws ExecutionException {
    FileDocumentManager.getInstance().saveAllDocuments();
    JstdRunProfileState jstdState = JstdRunProfileState.cast(state);
    ExecutionResult executionResult = jstdState.executeWithServer(server);
    RunContentBuilder contentBuilder = new RunContentBuilder(executionResult, environment);
    final RunContentDescriptor descriptor = contentBuilder.showRunContent(environment.getContentToReuse());
    if (server != null && executionResult.getProcessHandler() instanceof NopProcessHandler) {
      server.addLifeCycleListener(new JstdServerLifeCycleAdapter() {
        @Override
        public void onBrowserCaptured(@NotNull JstdBrowserInfo info) {
          if (fromDebug) {
            final Alarm alarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, descriptor);
            alarm.addRequest(() -> ExecutionUtil.restartIfActive(descriptor), 1000);
          }
          else {
            ExecutionUtil.restartIfActive(descriptor);
          }
          server.removeLifeCycleListener(this);
        }
      }, contentBuilder);
    }
    return descriptor;
  }
}
