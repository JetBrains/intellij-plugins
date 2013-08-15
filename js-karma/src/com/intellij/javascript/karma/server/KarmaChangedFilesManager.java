package com.intellij.javascript.karma.server;

import com.intellij.openapi.vfs.VirtualFile;
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
    OutputStream outputStream = server.getProcessOutputArchive().getProcessHandler().getProcessInput();
    //noinspection IOResourceOpenedButNotSafelyClosed
    myServerProcessInput = new PrintWriter(outputStream, false);
  }

  public void onFileChanged(@NotNull VirtualFile file) {
    synchronized (LOCK) {
      myServerProcessInput.print("changed-file:");
      myServerProcessInput.print(file.getPath());
      myServerProcessInput.print("\n");
    }
  }

  public void onFileAdded(@NotNull String path) {
    synchronized (LOCK) {
      myServerProcessInput.print("added-file:");
      myServerProcessInput.print(path);
      myServerProcessInput.print("\n");
    }
  }

  public void onFileRemoved(@NotNull String path) {
    synchronized (LOCK) {
      myServerProcessInput.print("removed-file:");
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
