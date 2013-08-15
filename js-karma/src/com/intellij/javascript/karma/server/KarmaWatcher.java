package com.intellij.javascript.karma.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class KarmaWatcher {

  private final KarmaServer myServer;
  private volatile KarmaWatchSession mySession;

  public KarmaWatcher(@NotNull KarmaServer server) {
    myServer = server;
  }

  @NotNull
  public StreamEventHandler getEventHandler() {
    return new StreamEventHandler() {
      @NotNull
      @Override
      public String getEventType() {
        return "configFilePatterns";
      }

      @Override
      public void handle(@NotNull JsonElement eventBody) {
        JsonArray patterns = eventBody.getAsJsonArray();
        final List<String> paths = ContainerUtil.newArrayListWithExpectedSize(patterns.size());
        for (JsonElement pattern : patterns) {
          JsonPrimitive p = pattern.getAsJsonPrimitive();
          paths.add(p.getAsString());
        }
        ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
          @Override
          public void run() {
            mySession = new KarmaWatchSession(myServer, paths);
          }
        });
      }
    };
  }

  public void flush() {
    if (mySession != null) {
      mySession.flush();
    }
  }

  public void stop() {
    if (mySession != null) {
      mySession.stop();
    }
  }

}
