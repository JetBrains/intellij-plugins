package org.jetbrains.jps.osmorc.build;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Reporter {
  void progress(@NotNull String message);

  void warning(@NotNull String message, @Nullable Throwable t, @Nullable String sourcePath);

  void error(@NotNull String message, @Nullable Throwable t, @Nullable String sourcePath);
}