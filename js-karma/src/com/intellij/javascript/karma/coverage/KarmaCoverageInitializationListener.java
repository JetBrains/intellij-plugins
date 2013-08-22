package com.intellij.javascript.karma.coverage;

import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public interface KarmaCoverageInitializationListener {
  void onCoverageInitialized(@NotNull KarmaCoverageStartupStatus initStatus);
}
