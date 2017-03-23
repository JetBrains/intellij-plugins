package com.intellij.flex.flexunit.execution;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.lang.javascript.flex.run.FlexRunner;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class FlexUnitTestRunner extends FlexRunner {
  @Override
  public void execute(@NotNull final ExecutionEnvironment env, @Nullable final Callback callback) throws ExecutionException {
    final Project project = env.getProject();
    final RunProfileState state = env.getState();
    if (state == null) {
      return;
    }

    Runnable startRunnable = () -> {
      try {
        if (project.isDisposed()) return;

        final RunContentDescriptor descriptor = doExecute(state, env);
        if (callback != null) callback.processStarted(descriptor);

        if (descriptor != null) {
          ExecutionManager.getInstance(project).getContentManager().showRunContent(env.getExecutor(), descriptor);
          final ProcessHandler processHandler = descriptor.getProcessHandler();
          if (processHandler != null) processHandler.startNotify();
        }
      }
      catch (ExecutionException e) {
        ExecutionUtil.handleExecutionError(env, e);
      }
    };

    ExecutionManager.getInstance(project).compileAndRun(startRunnable, env, state, null);
  }
}
