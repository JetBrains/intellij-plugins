package com.intellij.javascript.karma.util;

import org.jetbrains.annotations.NotNull;

public interface StreamEventListener {
  void on(@NotNull String eventType, @NotNull String eventBody);
}
