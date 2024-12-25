package com.jetbrains.plugins.meteor.runner;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.KillableColoredProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class MeteorMainProcessHandler extends KillableColoredProcessHandler {
  private static final int QUEUE_LIMIT = 300;

  private volatile InetSocketAddress mySocketAddress;
  private final Deque<Pair<String, Key>> myLastMessage = new ArrayDeque<>(QUEUE_LIMIT);
  private final Object myLock = new Object();

  public MeteorMainProcessHandler(@NotNull GeneralCommandLine commandLine) throws ExecutionException {
    super(commandLine);
    addProcessListener(new ProcessAdapter() {
      @Override
      public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
        addMessage(event, outputType);
      }
    });
  }

  public @NotNull List<Pair<String, Key>> getLastMessage() {
    synchronized (myLock) {
      return new ArrayList<>(myLastMessage);
    }
  }

  private void addMessage(ProcessEvent event, Key outputType) {
    synchronized (myLock) {
      while ((myLastMessage.size() >= QUEUE_LIMIT)) {
        myLastMessage.pop();
      }

      myLastMessage.add(Pair.create(event.getText(), outputType));
    }
  }

  public InetSocketAddress getSocketAddress() {
    return mySocketAddress;
  }

  public void setSocketAddress(InetSocketAddress socketAddress) {
    this.mySocketAddress = socketAddress;
  }

  @Override
  protected void doDestroyProcess() {
    super.doDestroyProcess();
    if (SystemInfo.isWindows) {
      killProcess();
    }
  }
}
