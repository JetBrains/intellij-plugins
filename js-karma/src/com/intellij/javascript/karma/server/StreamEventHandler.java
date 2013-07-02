package com.intellij.javascript.karma.server;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public interface StreamEventHandler {
  @NotNull
  String getEventType();

  void handle(@NotNull JsonElement eventBody);
}
