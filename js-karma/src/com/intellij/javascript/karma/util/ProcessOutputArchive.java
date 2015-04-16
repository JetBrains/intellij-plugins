package com.intellij.javascript.karma.util;

import com.google.common.collect.Lists;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProcessOutputArchive {

  public static final char NEW_LINE = '\n';
  private static final String PREFIX = "##intellij-event[";
  private static final String SUFFIX = "]\n";

  private final ProcessHandler myProcessHandler;
  private final List<Pair<String, Key>> myTexts = Lists.newArrayList();
  private final List<ArchivedOutputListener> myOutputListeners = new CopyOnWriteArrayList<ArchivedOutputListener>();
  private final List<StreamEventListener> myStdOutStreamEventListeners = new CopyOnWriteArrayList<StreamEventListener>();
  private final StringBuilder myStdOutCurrentLineBuffer = new StringBuilder();
  private final List<Pair<String, Key>> myStdOutCurrentLineChunks = ContainerUtil.newArrayList();

  public ProcessOutputArchive(@NotNull ProcessHandler processHandler) {
    myProcessHandler = processHandler;
  }

  public void startNotify() {
    myProcessHandler.addProcessListener(new ProcessAdapter() {
      @Override
      public void onTextAvailable(ProcessEvent event, Key outputType) {
        String text = event.getText();
        if (outputType != ProcessOutputTypes.SYSTEM && outputType != ProcessOutputTypes.STDERR) {
          processStandardOutput(text, outputType);
        }
        else {
          addText(text, outputType);
        }
      }
    });
    myProcessHandler.startNotify();
  }

  private void processStandardOutput(@NotNull String text, @NotNull Key type) {
    int newLineInd = text.indexOf(NEW_LINE);
    if (newLineInd == -1) {
      myStdOutCurrentLineBuffer.append(text);
      myStdOutCurrentLineChunks.add(Pair.create(text, type));
      return;
    }
    String beforeNewLineText = text.substring(0, newLineInd + 1); // always not empty
    myStdOutCurrentLineBuffer.append(beforeNewLineText);
    myStdOutCurrentLineChunks.add(Pair.create(beforeNewLineText, type));
    String line = myStdOutCurrentLineBuffer.toString();
    if (!handleLineAsEvent(line)) {
      onStandardOutputLineAvailable(line);
      for (Pair<String, Key> chunk : myStdOutCurrentLineChunks) {
        addText(chunk.getFirst(), chunk.getSecond());
      }
    }
    myStdOutCurrentLineBuffer.setLength(0);
    myStdOutCurrentLineChunks.clear();
    int prevNewLineInd = newLineInd;
    newLineInd = text.indexOf(NEW_LINE, prevNewLineInd + 1);
    while (newLineInd != -1) {
      line = text.substring(prevNewLineInd + 1, newLineInd + 1);
      if (!handleLineAsEvent(line)) {
        onStandardOutputLineAvailable(line);
        addText(line, type);
      }
      prevNewLineInd = newLineInd;
      newLineInd = text.indexOf(NEW_LINE, prevNewLineInd + 1);
    }
    if (prevNewLineInd + 1 < text.length()) {
      String rest = text.substring(prevNewLineInd + 1);
      myStdOutCurrentLineBuffer.append(rest);
      myStdOutCurrentLineChunks.add(Pair.create(rest, type));
    }
  }

  private void addText(@NotNull String text, @NotNull Key outputType) {
    synchronized (myTexts) {
      myTexts.add(Pair.create(text, outputType));
      for (ArchivedOutputListener listener : myOutputListeners) {
        listener.onOutputAvailable(text, outputType, false);
      }
    }
  }

  protected void onStandardOutputLineAvailable(@NotNull String line) {
  }

  private boolean handleLineAsEvent(@NotNull String line) {
    if (line.startsWith(PREFIX) && line.endsWith(SUFFIX)) {
      int colonInd = line.indexOf(':');
      if (colonInd == -1) {
        return false;
      }
      String eventType = line.substring(PREFIX.length(), colonInd);
      String eventBody = line.substring(colonInd + 1, line.length() - SUFFIX.length());
      for (StreamEventListener listener : myStdOutStreamEventListeners) {
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
    myStdOutStreamEventListeners.add(listener);
  }

}
