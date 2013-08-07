package com.intellij.javascript.karma.server;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author Sergey Simonchik
 */
public class KarmaChangedFilesManager {

  private final Object LOCK = new Object();
  private final KarmaServer myServer;
  private final PrintWriter myServerProcessInput;
  //private final ExecutorService myService;
  private final Set<String> myChangedFilePaths = ContainerUtil.newHashSet();
  private final Set<String> myAddedFilePaths = ContainerUtil.newHashSet();
  private final Set<String> myRemovedFilePaths = ContainerUtil.newHashSet();

  public KarmaChangedFilesManager(KarmaServer server) {
    myServer = server;
    OutputStream outputStream = server.getProcessOutputArchive().getProcessHandler().getProcessInput();
    //noinspection IOResourceOpenedButNotSafelyClosed
    myServerProcessInput = new PrintWriter(outputStream, false);
    BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
    ThreadFactoryBuilder threadFactoryBuilder = new ThreadFactoryBuilder();
    threadFactoryBuilder.setDaemon(false);
    threadFactoryBuilder.setDaemon(false);
    threadFactoryBuilder.setNameFormat("karma-changed-files-tracker");
    //myService = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, queue, threadFactoryBuilder.build());
  }

  public void onFileChanged(@NotNull VirtualFile file) {
    System.out.println("fileChanged:" + file.getPath());
    synchronized (LOCK) {
      myServerProcessInput.print("changed-file:");
      myServerProcessInput.print(file.getPath());
      myServerProcessInput.print("\n");
    }
  }

  public void onFileAdded(@NotNull String path) {
    System.out.println("fileAdded:" + path);
    synchronized (LOCK) {
      myServerProcessInput.print("added-file:");
      myServerProcessInput.print(path);
      myServerProcessInput.print("\n");
    }
  }

  public void onFileRemoved(@NotNull String path) {
    System.out.println("fileRemoved:" + path);
    synchronized (LOCK) {
      myServerProcessInput.print("removed-file:");
      myServerProcessInput.print(path);
      myServerProcessInput.print("\n");
    }
  }

  public void flush() {
    System.out.println("Flushing");
    synchronized (LOCK) {
      myServerProcessInput.flush();
    }
  }

}
