package com.intellij.javascript.karma.server;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;

public interface StreamEventHandler {
  @NotNull
  String getEventType();

  void handle(@NotNull JsonElement eventBody);
}
