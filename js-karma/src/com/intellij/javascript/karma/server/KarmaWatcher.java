package com.intellij.javascript.karma.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
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
public class KarmaWatcher {

  private static final Logger LOG = Logger.getInstance(KarmaWatcher.class);
  private static final Pattern BASE_DIR_PATTERN = Pattern.compile("/[^/]*[*(].*$");
  private final KarmaServer myServer;

  public KarmaWatcher(@NotNull KarmaServer server) {
    myServer = server;
  }

  private void startWatching(@NotNull final List<String> paths) {
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      @Override
      public void run() {
        final List<LocalFileSystem.WatchRequest> watchRequests = ContainerUtil.newArrayList();
        final LocalFileSystem fileSystem = LocalFileSystem.getInstance();
        ApplicationManager.getApplication().runReadAction(new Runnable() {
          @Override
          public void run() {
            for (String path : paths) {
              LocalFileSystem.WatchRequest watchRequest = doWatch(fileSystem, path);
              if (watchRequest != null) {
                watchRequests.add(watchRequest);
              }
            }
            listenForChanges(fileSystem);
          }
        });
        myServer.doWhenTerminated(new KarmaServerTerminatedListener() {
          @Override
          public void onTerminated(int exitCode) {
            ApplicationManager.getApplication().runReadAction(new Runnable() {
              @Override
              public void run() {
                for (LocalFileSystem.WatchRequest watchRequest : watchRequests) {
                  fileSystem.removeWatchedRoot(watchRequest);
                }
              }
            });
          }
        });
      }
    });
  }

  private void listenForChanges(@NotNull LocalFileSystem fileSystem) {
    fileSystem.addVirtualFileListener(new VirtualFileListener() {
      @Override
      public void propertyChanged(VirtualFilePropertyEvent event) {
        // ignored
      }

      @Override
      public void contentsChanged(VirtualFileEvent event) {
        System.out.println("contentsChanged(" + event.getFile().getPath() + ")");
      }

      @Override
      public void fileCreated(VirtualFileEvent event) {
        System.out.println("fileCreated(" + event.getFile().getPath() + ")");

      }

      @Override
      public void fileDeleted(VirtualFileEvent event) {
        System.out.println("fileDeleted(" + event.getFile().getPath() + ")");
      }

      @Override
      public void fileMoved(VirtualFileMoveEvent event) {
        System.out.println("fileMoved(" + event.getFile().getPath() + ")");
      }

      @Override
      public void fileCopied(VirtualFileCopyEvent event) {
        System.out.println("fileCopied(" + event.getFile().getPath() + ")");
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
    });
  }

  @Nullable
  private static LocalFileSystem.WatchRequest doWatch(@NotNull LocalFileSystem fileSystem, @NotNull String path) {
    LOG.info("Starting watching " + path);
    String vfsPath = path.replace(File.separatorChar, '/');
    final VirtualFile file = fileSystem.findFileByPath(vfsPath);
    final LocalFileSystem.WatchRequest watchRequest;
    if (file != null && file.isValid()) {
      watchRequest = watchDirectory(fileSystem, file);
    }
    else {
      String baseDirPath = BASE_DIR_PATTERN.matcher(vfsPath).replaceFirst("");
      if (baseDirPath.isEmpty()) {
        baseDirPath = "/";
      }
      final VirtualFile baseDir = fileSystem.findFileByPath(baseDirPath);
      if (baseDir != null && baseDir.isValid()) {
        watchRequest = watchDirectory(fileSystem, baseDir);
      }
      else {
        LOG.warn("Can not identify watch pattern for " + baseDirPath);
        watchRequest = null;
      }
    }
    if (watchRequest == null) {
      LOG.warn("Can not watch " + path);
    }
    return watchRequest;
  }

  private static LocalFileSystem.WatchRequest watchDirectory(@NotNull LocalFileSystem fileSystem, @NotNull VirtualFile dir) {
    VfsUtilCore.visitChildrenRecursively(dir, new VirtualFileVisitor() {
      @Override
      public boolean visitFile(@NotNull VirtualFile file) {
        file.getChildren();
        return true;
      }
    });
    LocalFileSystem.WatchRequest watchRequest = fileSystem.addRootToWatch(dir.getPath(), true);
    if (watchRequest == null) {
      LOG.error("Can not watch valid directory " + dir.getPath());
    }
    return watchRequest;
  }

  @NotNull
  public StreamEventHandler getEventHandler() {
    return new StreamEventHandler() {
      @NotNull
      @Override
      public String getEventType() {
        return "config-file-patterns";
      }

      @Override
      public void handle(@NotNull JsonElement eventBody) {
        JsonArray patterns = eventBody.getAsJsonArray();
        List<String> paths = ContainerUtil.newArrayListWithExpectedSize(patterns.size());
        for (JsonElement pattern : patterns) {
          JsonPrimitive p = pattern.getAsJsonPrimitive();
          paths.add(p.getAsString());
        }
        startWatching(paths);
      }
    };
  }

}
