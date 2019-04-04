package com.google.jstestdriver.idea.server;

import org.jetbrains.annotations.NotNull;

public class JstdServerLifeCycleAdapter implements JstdServerLifeCycleListener {
  @Override
  public void onServerStarted() {}

  @Override
  public void onServerStopped() {}

  @Override
  public void onServerTerminated(int exitCode) {}

  @Override
  public void onBrowserCaptured(@NotNull JstdBrowserInfo info) {}

  @Override
  public void onBrowserPanicked(@NotNull JstdBrowserInfo info) {}
}
