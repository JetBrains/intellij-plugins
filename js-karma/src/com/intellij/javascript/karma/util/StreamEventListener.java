package com.intellij.javascript.karma.util;

import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public interface StreamEventListener {
  void on(@NotNull String eventText);
}
