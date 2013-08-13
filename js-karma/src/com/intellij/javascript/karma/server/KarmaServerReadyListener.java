package com.intellij.javascript.karma.server;

/**
 * @author Sergey Simonchik
 */
public interface KarmaServerReadyListener {
  void onReady(int serverPort);
}
