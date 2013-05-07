package com.intellij.javascript.karma.util;

import com.google.common.collect.Lists;
import com.intellij.execution.process.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Sergey Simonchik
 */
public class ProcessEventStore {

  private static final String PREFIX = "##intellij-event[";
  private static final String SUFFIX = "]\n";

  private final ProcessHandler myProcessHandler;
  private final List<Pair<ProcessEvent, Key>> myTexts = Lists.newArrayList();
  private final List<ProcessListener> myProcessListeners = new CopyOnWriteArrayList<ProcessListener>();
  private final List<StreamEventListener> myStdoutStreamEventListeners = new CopyOnWriteArrayList<StreamEventListener>();

  public ProcessEventStore(@NotNull ProcessHandler processHandler) {
    myProcessHandler = processHandler;
  }

  public void startNotify() {
    myProcessHandler.addProcessListener(new ProcessAdapter() {
      @Override
      public void onTextAvailable(ProcessEvent event, Key outputType) {
        if (outputType == ProcessOutputTypes.STDOUT) {
          if (handleLineAsEvent(event.getText())) {
            return;
          }
        }
        synchronized (myTexts) {
          myTexts.add(Pair.create(event, outputType));
          for (ProcessListener listener : myProcessListeners) {
            listener.onTextAvailable(event, outputType);
          }
        }
      }
    });
    myProcessHandler.startNotify();
  }

  private boolean handleLineAsEvent(@NotNull String line) {
    if (line.startsWith(PREFIX)) {
      if (line.endsWith(SUFFIX)) {
        String eventText = line.substring(PREFIX.length(), line.length() - SUFFIX.length());
        for (StreamEventListener listener : myStdoutStreamEventListeners) {
          listener.on(eventText);
        }
        return true;
      }
    }
    return false;
  }

  @NotNull
  public ProcessHandler getProcessHandler() {
    return myProcessHandler;
  }

  public void addProcessListener(@NotNull final ProcessListener processListener) {
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      @Override
      public void run() {
        synchronized (myTexts) {
          for (Pair<ProcessEvent, Key> text : myTexts) {
            processListener.onTextAvailable(text.getFirst(), text.getSecond());
          }
          myProcessListeners.add(processListener);
        }
      }
    });
  }

  public void removeProcessListener(@NotNull final ProcessListener processListener) {
    synchronized (myTexts) {
      myProcessListeners.remove(processListener);
    }
  }

  public void addStreamEventListener(@NotNull StreamEventListener listener) {
    myStdoutStreamEventListeners.add(listener);
  }

  public void removeStreamEventListener(@NotNull StreamEventListener listener) {
    myStdoutStreamEventListeners.remove(listener);
  }

}
