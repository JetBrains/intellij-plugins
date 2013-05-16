package com.intellij.javascript.karma.util;

import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessListener;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Sergey Simonchik
 */
public class DelegatingProcessHandler extends ProcessHandler {

  private static final Logger LOG = Logger.getInstance(DelegatingProcessHandler.class);

  private final AtomicReference<ProcessHandler> myDelegateRef = new AtomicReference<ProcessHandler>(null);
  private volatile boolean myDestroyed = false;

  @Override
  protected void destroyProcessImpl() {
    myDestroyed = true;
    ProcessHandler peer = myDelegateRef.get();
    if (peer != null) {
      peer.destroyProcess();
    }
  }

  public void onDelegateTerminated(int exitCode) {
    notifyProcessTerminated(exitCode);
  }

  @Override
  protected void detachProcessImpl() {
    ProcessHandler peer = myDelegateRef.get();
    if (peer != null) {
      peer.detachProcess();
    }
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

  public void setDelegate(@NotNull ProcessHandler delegate) {
    if (myDelegateRef.compareAndSet(null, delegate)) {
      if (myDestroyed) {
        delegate.destroyProcess();
      }
      delegate.addProcessListener(new ProcessListener() {
        @Override
        public void startNotified(ProcessEvent event) {
          // this event has already been fired
        }

        @Override
        public void processTerminated(ProcessEvent event) {
          notifyProcessTerminated(event.getExitCode());
        }

        @Override
        public void processWillTerminate(ProcessEvent event, boolean willBeDestroyed) {
          // does nothing
        }

        @Override
        public void onTextAvailable(ProcessEvent event, Key outputType) {
          notifyTextAvailable(event.getText(), outputType);
        }
      });
    }
    else {
      LOG.error("Delegate has been already set");
    }
  }

}
