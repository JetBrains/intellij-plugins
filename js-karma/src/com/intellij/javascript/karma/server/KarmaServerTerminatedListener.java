package com.intellij.javascript.karma.server;

/**
 * @author Sergey Simonchik
 */
public interface KarmaServerTerminatedListener {
  void onTerminated(int exitCode);
}
