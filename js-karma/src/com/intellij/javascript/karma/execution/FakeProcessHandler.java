package com.intellij.javascript.karma.execution;

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
public class FakeProcessHandler extends ProcessHandler {

  private static final Logger LOG = Logger.getInstance(FakeProcessHandler.class);
  private final AtomicReference<ProcessHandler> myPeerRef = new AtomicReference<ProcessHandler>(null);
  private volatile boolean myDestroyed = false;

  @Override
  protected void destroyProcessImpl() {
    ProcessHandler peer = myPeerRef.get();
    if (peer != null) {
      peer.destroyProcess();
    }
    myDestroyed = true;
  }

  @Override
  protected void detachProcessImpl() {
    ProcessHandler peer = myPeerRef.get();
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

  public void setPeer(@NotNull ProcessHandler peer) {
    if (myPeerRef.compareAndSet(null, peer)) {
      if (myDestroyed) {
        peer.detachProcess();
      }
      peer.addProcessListener(new ProcessListener() {
        @Override
        public void startNotified(ProcessEvent event) {
          // this event has already been emitted
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
      LOG.error("Can't set peer twice");
    }
  }
}
