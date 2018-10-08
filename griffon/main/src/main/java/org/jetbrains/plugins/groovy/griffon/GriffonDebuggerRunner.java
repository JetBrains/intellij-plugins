// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.groovy.griffon;

import com.intellij.debugger.engine.DebuggerUtils;
import com.intellij.debugger.impl.GenericDebuggerRunner;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.*;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import org.jetbrains.annotations.NotNull;

/**
 * @author peter
 */
public class GriffonDebuggerRunner extends GenericDebuggerRunner {
  @Override
  public boolean canRun(@NotNull final String executorId, @NotNull final RunProfile profile) {
    return executorId.equals(DefaultDebugExecutor.EXECUTOR_ID) && profile instanceof GriffonRunConfiguration;
  }

  @Override
  @NotNull
  public String getRunnerId() {
    return "GriffonDebugger";
  }


  @Override
  protected RunContentDescriptor createContentDescriptor(@NotNull RunProfileState state, @NotNull ExecutionEnvironment environment) throws ExecutionException {
    final JavaCommandLine javaCommandLine = (JavaCommandLine)state;
    final JavaParameters params = javaCommandLine.getJavaParameters();

    if (!params.getVMParametersList().hasProperty("griffon.full.stacktrace")) {
      params.getVMParametersList().add("-Dgriffon.full.stacktrace=true");
    }

    String address = null;
    try {
      for (String s : params.getProgramParametersList().getList()) {
        if (s.startsWith("run-")) {
          // Application will be run in forked VM
          address = DebuggerUtils.getInstance().findAvailableDebugAddress(true);
          params.getProgramParametersList().replaceOrAppend(s, s + " --debug --debugPort=" + address);
          break;
        }
      }
    }
    catch (ExecutionException ignored) {
    }

    if (address == null) {
      return super.createContentDescriptor(state, environment);
    }
    return attachVirtualMachine(state, environment, new RemoteConnection(true, "127.0.0.1", address, false), true);
  }
}