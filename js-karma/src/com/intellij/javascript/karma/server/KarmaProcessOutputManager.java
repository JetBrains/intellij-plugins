package com.intellij.javascript.karma.server;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.javascript.karma.util.ArchivedOutputListener;
import com.intellij.javascript.karma.util.StreamEventListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.util.Consumer;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class KarmaProcessOutputManager {

  private static final int MAX_ARCHIVED_TEXTS_LENGTH = 1024 * 16;

  private static final char NEW_LINE = '\n';
  private static final String PREFIX = "##intellij-event[";
  private static final String SUFFIX = "]\n";

  private final ProcessHandler myProcessHandler;
  private final Deque<Pair<String, Key>> myArchivedTexts = new ArrayDeque<>();
  private int myArchivedTextsLength = 0;
  private boolean myArchiveTextsTruncated = false;
  private final List<ArchivedOutputListener> myOutputListeners = new CopyOnWriteArrayList<>();
  private final List<StreamEventListener> myStdOutStreamEventListeners = new CopyOnWriteArrayList<>();
  private final List<Pair<String, Key>> myStdOutCurrentLineChunks = ContainerUtil.newArrayList();
  private final Consumer<String> myStdOutLineConsumer;

  public KarmaProcessOutputManager(@NotNull ProcessHandler processHandler, @NotNull Consumer<String> stdOutLineConsumer) {
    myProcessHandler = processHandler;
    myStdOutLineConsumer = stdOutLineConsumer;
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
    int lineStartInd = 0;
    int newLineInd = text.indexOf(NEW_LINE, lineStartInd);
    while (newLineInd != -1) {
      String line = text.substring(lineStartInd, newLineInd + 1); // always not empty
      if (!myStdOutCurrentLineChunks.isEmpty()) {
        myStdOutCurrentLineChunks.add(Pair.create(line, type));
        line = concatCurrentLineChunks();
      }
      if (!handleLineAsEvent(line)) {
        onStandardOutputLineAvailable(line);
        if (!myStdOutCurrentLineChunks.isEmpty()) {
          for (Pair<String, Key> chunk : myStdOutCurrentLineChunks) {
            addText(chunk.getFirst(), chunk.getSecond());
          }
        }
        else {
          addText(line, type);
        }
      }
      myStdOutCurrentLineChunks.clear();
      lineStartInd = newLineInd + 1;
      newLineInd = text.indexOf(NEW_LINE, lineStartInd);
    }
    if (lineStartInd < text.length()) {
      myStdOutCurrentLineChunks.add(Pair.create(text.substring(lineStartInd), type));
    }
  }

  @NotNull
  private String concatCurrentLineChunks() {
    int size = 0;
    for (Pair<String, Key> chunk : myStdOutCurrentLineChunks) {
      size += chunk.getFirst().length();
    }
    StringBuilder result = new StringBuilder(size);
    for (Pair<String, Key> chunk : myStdOutCurrentLineChunks) {
      result.append(chunk.getFirst());
    }
    return result.toString();
  }

  private void addText(@NotNull String text, @NotNull Key outputType) {
    synchronized (myArchivedTexts) {
      myArchivedTexts.addLast(Pair.create(text, outputType));
      myArchivedTextsLength += text.length();
      while (myArchivedTextsLength > MAX_ARCHIVED_TEXTS_LENGTH) {
        Pair<String, Key> pair = myArchivedTexts.removeFirst();
        myArchivedTextsLength -= pair.getFirst().length();
        myArchiveTextsTruncated = true;
      }
      for (ArchivedOutputListener listener : myOutputListeners) {
        listener.onOutputAvailable(text, outputType, false);
      }
    }
  }

  private void onStandardOutputLineAvailable(@NotNull String line) {
    myStdOutLineConsumer.consume(line);
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
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      synchronized (myArchivedTexts) {
        if (myArchiveTextsTruncated) {
          outputListener.onOutputAvailable("... too much output to process, truncated\n", ProcessOutputTypes.SYSTEM, true);
        }
        for (Pair<String, Key> text : myArchivedTexts) {
          outputListener.onOutputAvailable(text.getFirst(), text.getSecond(), true);
        }
        myOutputListeners.add(outputListener);
      }
    });
  }

  public void removeOutputListener(@NotNull ArchivedOutputListener outputListener) {
    myOutputListeners.remove(outputListener);
  }

  void addStreamEventListener(@NotNull StreamEventListener listener) {
    myStdOutStreamEventListeners.add(listener);
  }

}
