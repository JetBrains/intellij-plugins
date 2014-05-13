package com.google.jstestdriver.idea.server;

import com.google.gson.JsonObject;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

public interface JstdServerOutputListener {
  void onOutputAvailable(@NotNull String text, @NotNull Key outputType);
  void onEvent(@NotNull JsonObject obj);
}
