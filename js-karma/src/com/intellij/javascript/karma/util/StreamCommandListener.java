package com.intellij.javascript.karma.util;

import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public interface StreamCommandListener {
  void onCommand(@NotNull String commandName);
}
