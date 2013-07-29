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
public class ProcessOutputArchive {

  private static final String PREFIX = "##intellij-event[";
  private static final String SUFFIX = "]\n";

  private final ProcessHandler myProcessHandler;
  private final List<Pair<String, Key>> myTexts = Lists.newArrayList();
  private final List<ArchivedOutputListener> myOutputListeners = new CopyOnWriteArrayList<ArchivedOutputListener>();
  private final List<StreamEventListener> myStdoutStreamEventListeners = new CopyOnWriteArrayList<StreamEventListener>();

  public ProcessOutputArchive(@NotNull ProcessHandler processHandler) {
    myProcessHandler = processHandler;
  }

  public void startNotify() {
    myProcessHandler.addProcessListener(new ProcessAdapter() {
      @Override
      public void onTextAvailable(ProcessEvent event, Key outputType) {
        String text = event.getText();
        if (outputType != ProcessOutputTypes.SYSTEM && outputType != ProcessOutputTypes.STDERR) {
          if (handleLineAsEvent(text)) {
            return;
          }
        }
        synchronized (myTexts) {
          myTexts.add(Pair.create(text, outputType));
          for (ArchivedOutputListener listener : myOutputListeners) {
            listener.onOutputAvailable(event.getText(), outputType, false);
          }
        }
      }
    });
    myProcessHandler.startNotify();
  }

  private boolean handleLineAsEvent(@NotNull String line) {
    if (line.startsWith(PREFIX) && line.endsWith(SUFFIX)) {
      int colonInd = line.indexOf(':');
      if (colonInd == -1) {
        return false;
      }
      String eventType = line.substring(PREFIX.length(), colonInd);
      String eventBody = line.substring(colonInd + 1, line.length() - SUFFIX.length());
      for (StreamEventListener listener : myStdoutStreamEventListeners) {
        listener.on(eventType, eventBody);
      }
      return true;
    }
    return false;
  }

  @NotNull
  public ProcessHandler getProcessHandler() {
    return myProcessHandler;
  }

  public void addOutputListener(@NotNull final ArchivedOutputListener outputListener) {
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      @Override
      public void run() {
        synchronized (myTexts) {
          for (Pair<String, Key> text : myTexts) {
            outputListener.onOutputAvailable(text.getFirst(), text.getSecond(), true);
          }
          myOutputListeners.add(outputListener);
        }
      }
    });
  }

  public void addStreamEventListener(@NotNull StreamEventListener listener) {
    myStdoutStreamEventListeners.add(listener);
  }

}
