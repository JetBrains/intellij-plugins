package com.intellij.javascript.karma.util;

import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public interface ArchivedOutputListener {
  void onOutputAvailable(@NotNull String text, Key outputType, boolean archived);
}
