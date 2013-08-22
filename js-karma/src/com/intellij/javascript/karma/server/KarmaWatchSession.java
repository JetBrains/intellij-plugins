package com.intellij.javascript.karma.server;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.*;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Sergey Simonchik
 */
public class KarmaWatchSession {

  private static final Logger LOG = Logger.getInstance(KarmaWatcher.class);
  private static final char SEPARATOR_CHAR = '/';
  private static final String SEPARATOR = String.valueOf(SEPARATOR_CHAR);

  private final KarmaChangedFilesManager myChangedFilesManager;
  private final LocalFileSystem myFileSystem;
  private final List<WatchPattern> myWatchPatterns = ContainerUtil.newArrayList();
  private final MyVirtualFileListener myVfsListener = new MyVirtualFileListener();

  public KarmaWatchSession(@NotNull KarmaServer server, @NotNull final List<String> paths) {
    myChangedFilesManager = new KarmaChangedFilesManager(server);
    myFileSystem = LocalFileSystem.getInstance();
    ApplicationManager.getApplication().runReadAction(new Runnable() {
      @Override
      public void run() {
        for (String path : paths) {
          WatchPattern watchPattern = new WatchPattern(myFileSystem, path);
          myWatchPatterns.add(watchPattern);
        }
        myFileSystem.addVirtualFileListener(myVfsListener);
      }
    });
  }

  private boolean insideRoots(@NotNull VirtualFile file) {
    for (WatchPattern watchPattern : myWatchPatterns) {
      VirtualFile root = watchPattern.myRoot;
      if (root != null && VfsUtilCore.isAncestor(root, file, false)) {
        return true;
      }
    }
    return false;
  }

  public void stop() {
    ApplicationManager.getApplication().runReadAction(new Runnable() {
      @Override
      public void run() {
        for (WatchPattern watchPattern : myWatchPatterns) {
          watchPattern.stopWatching();
        }
        myFileSystem.removeVirtualFileListener(myVfsListener);
      }
    });
  }

  public void flush() {
    myChangedFilesManager.flush();
  }

  private static String join(@NotNull String path1, @NotNull String path2) {
    if (path1.endsWith(SEPARATOR)) {
      path1 = path1.substring(0, path1.length() - 1);
    }
    if (path2.startsWith(SEPARATOR)) {
      path2 = path2.substring(1);
    }
    return path1 + SEPARATOR + path2;
  }

  private class MyVirtualFileListener implements VirtualFileListener {

    @Override
    public void propertyChanged(VirtualFilePropertyEvent event) {
      if ("name".equals(event.getPropertyName())) {
        VirtualFile parent = event.getParent();
        if (parent != null) {
          String oldPath = join(parent.getPath(), event.getOldValue().toString());
          String newPath = join(parent.getPath(), event.getNewValue().toString());
          if (insideRoots(parent)) {
            myChangedFilesManager.onFileRemoved(oldPath);
            myChangedFilesManager.onFileAdded(newPath);
          }
        }
      }
    }

    @Override
    public void contentsChanged(VirtualFileEvent event) {
      VirtualFile file = event.getFile();
      if (insideRoots(file)) {
        myChangedFilesManager.onFileChanged(file);
      }
    }

    @Override
    public void fileCreated(VirtualFileEvent event) {
      VirtualFile file = event.getFile();
      if (insideRoots(file)) {
        myChangedFilesManager.onFileAdded(file.getPath());
      }
    }

    @Override
    public void fileDeleted(VirtualFileEvent event) {
      VirtualFile file = event.getFile();
      if (insideRoots(file)) {
        myChangedFilesManager.onFileRemoved(file.getPath());
      }
    }

    @Override
    public void fileMoved(VirtualFileMoveEvent event) {
      String fileName = event.getFileName();
      VirtualFile oldParent = event.getOldParent();
      if (insideRoots(oldParent)) {
        String oldPath = join(oldParent.getPath(), fileName);
        myChangedFilesManager.onFileRemoved(oldPath);
      }
      VirtualFile newParent = event.getNewParent();
      if (insideRoots(newParent)) {
        String newPath = join(newParent.getPath(), fileName);
        myChangedFilesManager.onFileAdded(newPath);
      }
    }

    @Override
    public void fileCopied(VirtualFileCopyEvent event) {
      VirtualFile file = event.getFile();
      if (insideRoots(file)) {
        myChangedFilesManager.onFileAdded(file.getPath());
      }
    }

    @Override
    public void beforePropertyChange(VirtualFilePropertyEvent event) {
      // ignored
    }

    @Override
    public void beforeContentsChange(VirtualFileEvent event) {
      // ignored
    }

    @Override
    public void beforeFileDeletion(VirtualFileEvent event) {
      // ignored
    }

    @Override
    public void beforeFileMovement(VirtualFileMoveEvent event) {
      // ignored
    }
  }

  private static class WatchPattern {

    private static final Pattern BASE_DIR_PATTERN = Pattern.compile("/[^/]*[*(].*$");

    private final LocalFileSystem myFileSystem;
    private final String myVfsPath;
    private final String myBasePathDir;
    private final boolean myCheckBasePathDir;
    private LocalFileSystem.WatchRequest myWatchRequest;
    private VirtualFile myRoot;

    private WatchPattern(@NotNull LocalFileSystem fileSystem, @NotNull final String pattern) {
      LOG.info("Start watching path pattern " + pattern);

      myFileSystem = fileSystem;
      myVfsPath = pattern.replace(File.separatorChar, SEPARATOR_CHAR);
      String baseDirPath = BASE_DIR_PATTERN.matcher(myVfsPath).replaceFirst("");
      if (baseDirPath.isEmpty()) {
        baseDirPath = SEPARATOR;
      }
      myBasePathDir = baseDirPath;
      myCheckBasePathDir = !myVfsPath.equals(myBasePathDir);

      if (myRoot != null) {
        myWatchRequest = watchDirectory(myRoot);
      }
      else {
        LOG.warn("Can not find watch root for " + myBasePathDir);
      }
    }

    public void check() {
      if (myWatchRequest != null) {
        return;
      }
      if (myRoot == null || !myRoot.isValid()) {
        VirtualFile newRoot = null;
        final VirtualFile file = myFileSystem.findFileByPath(myVfsPath);
        if (file != null && file.isValid()) {
          newRoot = file;
        }
        else if (myCheckBasePathDir) {
          final VirtualFile baseDir = myFileSystem.findFileByPath(myBasePathDir);
          if (baseDir != null && baseDir.isValid()) {
            newRoot = baseDir;
          }
        }
        myRoot = newRoot;
      }
      if (myRoot != null) {
        myWatchRequest = watchDirectory(myRoot);
      }
    }

    @Nullable
    private LocalFileSystem.WatchRequest watchDirectory(@NotNull VirtualFile dir) {
      VfsUtilCore.visitChildrenRecursively(dir, new VirtualFileVisitor() {
        @Override
        public boolean visitFile(@NotNull VirtualFile file) {
          file.getChildren();
          return true;
        }
      });
      LocalFileSystem.WatchRequest watchRequest = myFileSystem.addRootToWatch(dir.getPath(), true);
      if (watchRequest == null) {
        LOG.warn("Can not watch valid directory " + dir.getPath());
      }
      return watchRequest;
    }

    public void stopWatching() {
      LocalFileSystem.WatchRequest watchRequest = myWatchRequest;
      if (watchRequest != null) {
        myFileSystem.removeWatchedRoot(watchRequest);
      }
    }
  }

}
