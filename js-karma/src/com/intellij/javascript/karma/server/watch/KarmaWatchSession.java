package com.intellij.javascript.karma.server.watch;

import com.google.common.collect.ImmutableList;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.*;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class KarmaWatchSession {

  public static final char SEPARATOR_CHAR = '/';
  public static final String SEPARATOR = String.valueOf(SEPARATOR_CHAR);

  private final KarmaChangedFilesManager myChangedFilesManager;
  private final LocalFileSystem myFileSystem;
  private final ImmutableList<KarmaWatchPattern> myWatchPatterns;
  private final MyVirtualFileListener myVfsListener = new MyVirtualFileListener();

  public KarmaWatchSession(@NotNull KarmaServer server, @NotNull final List<String> paths) {
    myChangedFilesManager = new KarmaChangedFilesManager(server);
    myFileSystem = LocalFileSystem.getInstance();
    final List<KarmaWatchPattern> watchPatterns = ContainerUtil.newArrayList();
    ApplicationManager.getApplication().runReadAction(() -> {
      for (String path : paths) {
        KarmaWatchPattern watchPattern = new KarmaWatchPattern(myFileSystem, myChangedFilesManager, path);
        watchPatterns.add(watchPattern);
      }
      myFileSystem.addVirtualFileListener(myVfsListener);
    });
    myWatchPatterns = ImmutableList.copyOf(watchPatterns);
  }

  @Nullable
  private String findWatchedOriginalPath(@NotNull VirtualFile file) {
    for (KarmaWatchPattern watchPattern : myWatchPatterns) {
      String watchedPath = watchPattern.findWatchedOriginalPath(file);
      if (watchedPath != null) {
        return watchedPath;
      }
    }
    return null;
  }

  private void updateWatchPatterns() {
    for (KarmaWatchPattern pattern : myWatchPatterns) {
      pattern.update(true);
    }
  }

  public void stop() {
    ApplicationManager.getApplication().runReadAction(() -> {
      for (KarmaWatchPattern watchPattern : myWatchPatterns) {
        watchPattern.stopWatching();
      }
      myFileSystem.removeVirtualFileListener(myVfsListener);
    });
  }

  public void flush() {
    myChangedFilesManager.flush();
  }

  @NotNull
  public static String join(@NotNull String path, @NotNull String subPath) {
    if (path.endsWith(SEPARATOR)) {
      path = path.substring(0, path.length() - 1);
    }
    if (subPath.startsWith(SEPARATOR)) {
      subPath = subPath.substring(1);
    }
    return path + SEPARATOR + subPath;
  }

  private class MyVirtualFileListener implements VirtualFileListener {

    @Override
    public void propertyChanged(@NotNull VirtualFilePropertyEvent event) {
      updateWatchPatterns();
      if ("name".equals(event.getPropertyName())) {
        VirtualFile parent = event.getParent();
        if (parent != null) {
          String parentWatchedPath = findWatchedOriginalPath(parent);
          if (parentWatchedPath != null) {
            String oldPath = join(parentWatchedPath, event.getOldValue().toString());
            String newPath = join(parentWatchedPath, event.getNewValue().toString());
            myChangedFilesManager.onFileRemoved(oldPath);
            myChangedFilesManager.onFileAdded(newPath);
          }
        }
      }
    }

    @Override
    public void contentsChanged(@NotNull VirtualFileEvent event) {
      VirtualFile file = event.getFile();
      String watchedPath = findWatchedOriginalPath(file);
      if (watchedPath != null) {
        myChangedFilesManager.onFileChanged(watchedPath);
      }
    }

    @Override
    public void fileCreated(@NotNull VirtualFileEvent event) {
      updateWatchPatterns();
      VirtualFile file = event.getFile();
      String watchedPath = findWatchedOriginalPath(file);
      if (watchedPath != null) {
        myChangedFilesManager.onFileAdded(watchedPath);
      }
    }

    @Override
    public void fileDeleted(@NotNull VirtualFileEvent event) {
      updateWatchPatterns();
      VirtualFile file = event.getFile();
      String watchedPath = findWatchedOriginalPath(file);
      if (watchedPath != null) {
        myChangedFilesManager.onFileRemoved(watchedPath);
      }
    }

    @Override
    public void fileMoved(@NotNull VirtualFileMoveEvent event) {
      updateWatchPatterns();
      String fileName = event.getFileName();
      VirtualFile oldParent = event.getOldParent();
      String oldParentWatchedPath = findWatchedOriginalPath(oldParent);
      if (oldParentWatchedPath != null) {
        String oldPath = join(oldParentWatchedPath, fileName);
        myChangedFilesManager.onFileRemoved(oldPath);
      }
      VirtualFile newParent = event.getNewParent();
      String newParentWatchedPath = findWatchedOriginalPath(newParent);
      if (newParentWatchedPath != null) {
        String newPath = join(newParentWatchedPath, fileName);
        myChangedFilesManager.onFileAdded(newPath);
      }
    }

    @Override
    public void fileCopied(@NotNull VirtualFileCopyEvent event) {
      updateWatchPatterns();
      VirtualFile file = event.getFile();
      String watchedPath = findWatchedOriginalPath(file);
      if (watchedPath != null) {
        myChangedFilesManager.onFileAdded(watchedPath);
      }
    }
  }
}
