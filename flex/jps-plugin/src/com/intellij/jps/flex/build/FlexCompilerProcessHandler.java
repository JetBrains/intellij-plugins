// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.jps.flex.build;

import com.intellij.execution.process.BaseOSProcessHandler;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.flex.FlexCommonUtils;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.incremental.CompileContext;

import java.nio.charset.Charset;

public class FlexCompilerProcessHandler extends BaseOSProcessHandler {
  private final FlexCompilerProcessHandler.MyProcessListener myListener;

  public FlexCompilerProcessHandler(final CompileContext context,
                                    final Process process,
                                    final boolean asc20,
                                    final String compilerName,
                                    @NotNull String commandLine) {
    super(process, commandLine, Charset.forName(FlexCommonUtils.SDK_TOOLS_ENCODING));

    myListener = new MyProcessListener(context, asc20, compilerName);
    addProcessListener(myListener);
  }

  public boolean isCompilationFailed() {
    return myListener.isCompilationFailed();
  }

  public boolean isCancelled() {
    return myListener.isCompilationCancelled();
  }

  private class MyProcessListener extends CompilerMessageHandlerBase implements ProcessListener {

    MyProcessListener(final CompileContext context, final boolean asc20, final String compilerName) {
      super(context, asc20, compilerName);
    }

    @Override
    public void onTextAvailable(final @NotNull ProcessEvent event, final @NotNull Key outputType) {
      handleText(event.getText());
    }

    @Override
    public void processTerminated(final @NotNull ProcessEvent event) {
      registerCompilationFinished();
    }

    @Override
    protected void onCancelled() {
      destroyProcess();
    }
  }
}
