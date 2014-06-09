package com.google.jstestdriver.idea.server;

import org.jetbrains.annotations.NotNull;

public interface JstdServerLifeCycleListener {
  void onServerStarted();
  void onBrowserCaptured(@NotNull JstdBrowserInfo info);
  void onBrowserPanicked(@NotNull JstdBrowserInfo info);
  void onServerStopped();
  void onServerTerminated(int exitCode);
}
