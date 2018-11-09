package com.intellij.javascript.karma.server;

public interface KarmaServerTerminatedListener {
  void onTerminated(int exitCode);
}
