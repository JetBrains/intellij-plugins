package com.google.jstestdriver.idea.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.jstestdriver.idea.rt.server.JstdServerConstants;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;

public class JstdServerOutputProcessor {

  private static final Logger LOG = Logger.getInstance(JstdServerOutputProcessor.class);
  private static final int LIMIT = 1000;

  private final Queue<Pair<String, Key>> myTexts = new LinkedList<Pair<String, Key>>();
  private final List<JstdServerOutputListener> myListeners = new CopyOnWriteArrayList<JstdServerOutputListener>();

  public JstdServerOutputProcessor(@NotNull ProcessHandler processHandler) {
    processHandler.addProcessListener(new ProcessAdapter() {
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
          if (myTexts.size() > LIMIT) {
            myTexts.poll();
          }
        }
        for (JstdServerOutputListener listener : myListeners) {
          listener.onOutputAvailable(text, outputType);
        }
      }
    });
  }

  public void addListener(@NotNull final JstdServerOutputListener listener) {
    myListeners.add(listener);
    for (Pair<String, Key> text : myTexts) {
      listener.onOutputAvailable(text.getFirst(), text.getSecond());
    }
  }

  private boolean handleLineAsEvent(@NotNull String line) {
    if (line.startsWith(JstdServerConstants.EVENT_PREFIX) && line.endsWith(JstdServerConstants.EVENT_SUFFIX)) {
      String json = line.substring(JstdServerConstants.EVENT_PREFIX.length(), line.length() - JstdServerConstants.EVENT_SUFFIX.length());
      LOG.info("Processing JsTestDriver event " + json);
      try {
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(json);
        if (jsonElement.isJsonObject()) {
          fireEvent(jsonElement.getAsJsonObject());
        }
        else {
          LOG.warn("Unexpected JsTestDriver event. Json root object expected. " + json);
        }
      }
      catch (Exception e) {
        LOG.warn("Cannot parse message from JsTestDriver server:" + json);
      }
      return true;
    }
    return false;
  }

  private void fireEvent(@NotNull JsonObject json) {
    for (JstdServerOutputListener listener : myListeners) {
      listener.onEvent(json);
    }
  }

  public void dispose() {
    myListeners.clear();
    myTexts.clear();
  }
}
