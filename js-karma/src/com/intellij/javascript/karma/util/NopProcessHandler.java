package com.intellij.javascript.karma.util;

import com.intellij.execution.process.ProcessHandler;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;

public class NopProcessHandler extends ProcessHandler {
  @Override
  protected void destroyProcessImpl() {
    notifyProcessTerminated(0);
  }

  @Override
  protected void detachProcessImpl() {
    notifyProcessDetached();
  }

  @Override
  public boolean detachIsDefault() {
    return false;
  }

  @Nullable
  @Override
  public OutputStream getProcessInput() {
    return null;
  }
}
