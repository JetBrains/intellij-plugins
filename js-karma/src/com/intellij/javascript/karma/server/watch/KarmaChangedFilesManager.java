package com.intellij.javascript.karma.server.watch;

import com.intellij.javascript.karma.server.KarmaServer;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * @author Sergey Simonchik
 */
public class KarmaChangedFilesManager {

  private final Object LOCK = new Object();
  private final PrintWriter myServerProcessInput;

  public KarmaChangedFilesManager(@NotNull KarmaServer server) {
    OutputStream outputStream = server.getProcessHandler().getProcessInput();
    //noinspection IOResourceOpenedButNotSafelyClosed
    myServerProcessInput = new PrintWriter(outputStream, false);
  }

  public void onFileChanged(@NotNull String path) {
    sendEvent("changed-file:", path);
  }

  public void onFileAdded(@NotNull String path) {
    sendEvent("added-file:", path);
  }

  public void onFileRemoved(@NotNull String path) {
    sendEvent("removed-file:", path);
  }

  private void sendEvent(@NotNull String prefix, @NotNull String path) {
    synchronized (LOCK) {
      myServerProcessInput.print(prefix);
      myServerProcessInput.print(path);
      myServerProcessInput.print("\n");
    }
  }

  public void flush() {
    synchronized (LOCK) {
      myServerProcessInput.flush();
    }
  }

}
