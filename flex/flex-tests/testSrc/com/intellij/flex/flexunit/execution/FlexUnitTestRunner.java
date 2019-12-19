// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.flexunit.execution;

import com.intellij.execution.ExecutionManager;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.lang.javascript.flex.run.FlexRunner;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class FlexUnitTestRunner extends FlexRunner {
  @Nullable
  @Override
  protected RunContentDescriptor doExecute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment environment) {
    Project project = environment.getProject();
    Runnable startRunnable = () -> {
      if (project.isDisposed()) {
        return;
      }

      RunContentDescriptor descriptor = doExecute(state, environment);
      if (descriptor != null) {
        RunContentManager.getInstance(project).showRunContent(environment.getExecutor(), descriptor);
        ProcessHandler processHandler = descriptor.getProcessHandler();
        if (processHandler != null) {
          processHandler.startNotify();
        }
      }
    };

    ExecutionManager.getInstance(project).compileAndRun(startRunnable, environment, null);
    return null;
  }
}
