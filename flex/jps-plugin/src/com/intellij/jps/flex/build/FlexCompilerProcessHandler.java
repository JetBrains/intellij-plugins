package com.intellij.jps.flex.build;

import com.intellij.execution.process.BaseOSProcessHandler;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.flex.FlexCommonUtils;
import com.intellij.openapi.util.Key;
import org.jetbrains.jps.incremental.CompileContext;

import java.nio.charset.Charset;

public class FlexCompilerProcessHandler extends BaseOSProcessHandler {
  private final FlexCompilerProcessHandler.MyProcessListener myListener;

  public FlexCompilerProcessHandler(final CompileContext context,
                                    final Process process,
                                    final boolean asc20,
                                    final String compilerName,
                                    final String commandLine) {
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

    public MyProcessListener(final CompileContext context, final boolean asc20, final String compilerName) {
      super(context, asc20, compilerName);
    }

    public void startNotified(final ProcessEvent event) {
    }

    public void onTextAvailable(final ProcessEvent event, final Key outputType) {
      handleText(event.getText());
    }

    public void processWillTerminate(final ProcessEvent event, final boolean willBeDestroyed) {
    }

    public void processTerminated(final ProcessEvent event) {
      registerCompilationFinished();
    }

    protected void onCancelled() {
      destroyProcess();
    }
  }
}
