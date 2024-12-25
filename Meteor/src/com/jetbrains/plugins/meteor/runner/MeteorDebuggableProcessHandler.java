package com.jetbrains.plugins.meteor.runner;

import com.intellij.execution.KillableProcess;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.ui.AppUIUtil;
import com.intellij.util.io.socketConnection.ConnectionStatus;
import com.intellij.xdebugger.DefaultDebugProcessHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.debugger.connection.RemoteVmConnection;

import java.io.OutputStream;
import java.util.List;

public class MeteorDebuggableProcessHandler extends DefaultDebugProcessHandler implements KillableProcess {
  private final @NotNull MeteorMainProcessHandler myMainProcessHandler;
  private volatile @Nullable ProcessAdapter myListener;
  private volatile RunContentDescriptor myRunContentDescriptor;
  private volatile RemoteVmConnection myVmConnection;
  private boolean myCalledDestroyParent;

  public boolean isCalledDestroyParent() {
    return myCalledDestroyParent;
  }

  public @NotNull MeteorMainProcessHandler getMainHandler() {
    return myMainProcessHandler;
  }

  MeteorDebuggableProcessHandler(@NotNull MeteorMainProcessHandler mainProcessHandler) {
    myMainProcessHandler = mainProcessHandler;
  }

  @Override
  public OutputStream getProcessInput() {
    return myMainProcessHandler.getProcessInput();
  }

  @Override
  public boolean detachIsDefault() {
    return false;
  }

  @Override
  public void destroyProcess() {
    //if destroy called after disconnecting -> disconnection was external
    boolean isExternalDisconnect = isParentAlive() && myVmConnection != null && myVmConnection.getState().getStatus() == ConnectionStatus.DISCONNECTED;
    if (!isExternalDisconnect) {
      myCalledDestroyParent = true;
      myMainProcessHandler.destroyProcess();
    }

    myMainProcessHandler.removeProcessListener(myListener);
    myVmConnection = null;
    super.destroyProcess();

    if (isExternalDisconnect) {
      //try to reconnect in a new session
      AppUIUtil.invokeOnEdt(() -> {
        if (isParentAlive()) {
          ExecutionUtil.restart(myRunContentDescriptor);
        }
      });
    }
  }

  @Override
  public boolean isProcessTerminated() {
    return !myMainProcessHandler.isProcessTerminating() && super.isProcessTerminated();
  }

  public void setRunContentDescriptor(RunContentDescriptor runContentDescriptor) {
    myRunContentDescriptor = runContentDescriptor;
  }

  public void setVmConnection(RemoteVmConnection vmConnection) {
    myVmConnection = vmConnection;
  }

  private boolean isParentAlive() {
    return MeteorDebugProcessRunner.Companion.isAliveProcessHandler(myMainProcessHandler);
  }

  @Override
  public boolean canKillProcess() {
    return myMainProcessHandler.canKillProcess();
  }

  @Override
  public void killProcess() {
    myMainProcessHandler.killProcess();
  }

  public void createTextMessagesListener() {
    myListener = MeteorDebugProcessRunner.Companion.getListenerForMainProcess(this);
    final List<Pair<String, Key>> messages = myMainProcessHandler.getLastMessage();
    for (Pair<String, Key> message : messages) {
      notifyTextAvailable(message.first, message.second);
    }

    myMainProcessHandler.addProcessListener(myListener);
  }
}
