package com.intellij.javascript.karma.server;

import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public interface KarmaServerListener {
  void onReady(int webServerPort, int runnerPort);
  void onTerminated(int exitCode);
  void onBrowserConnected(@NotNull String browserName);
  void onBrowserDisconnected(@NotNull String browserName);
}
