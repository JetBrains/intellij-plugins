package com.intellij.javascript.karma.server;

/**
 * @author Sergey Simonchik
 */
public interface KarmaServerListener {
  void onReady(int webServerPort, int runnerPort);
}
