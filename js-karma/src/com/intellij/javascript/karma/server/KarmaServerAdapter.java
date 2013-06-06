package com.intellij.javascript.karma.server;

import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public abstract class KarmaServerAdapter implements KarmaServerListener {

  @Override
  public void onReady(int webServerPort, int runnerPort) {}

  @Override
  public void onTerminated(int exitCode) {}

  @Override
  public void onBrowserConnected(@NotNull String browserName) {}

  @Override
  public void onBrowserDisconnected(@NotNull String browserName) {}

}
