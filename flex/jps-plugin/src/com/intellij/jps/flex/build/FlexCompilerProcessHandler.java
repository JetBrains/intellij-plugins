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
    public void startNotified(@NotNull ProcessEvent event) {
      ProcessListener.super.startNotified(event);
    }

    @Override
    public void onTextAvailable(@NotNull final ProcessEvent event, @NotNull final Key outputType) {
      handleText(event.getText());
    }

    @Override
    public void processWillTerminate(@NotNull ProcessEvent event, boolean willBeDestroyed) {
      ProcessListener.super.processWillTerminate(event, willBeDestroyed);
    }

    @Override
    public void processTerminated(@NotNull final ProcessEvent event) {
      registerCompilationFinished();
    }

    @Override
    protected void onCancelled() {
      destroyProcess();
    }
  }
}
