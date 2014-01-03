package com.intellij.javascript.karma.coverage;

import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public interface KarmaCoverageInitializationCallback {
  void onCoverageInitialized(@NotNull KarmaCoverageStartupStatus startupStatus);
}
