package com.intellij.javascript.karma.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Sergey Simonchik
 */
public class KarmaWatcher {

  private static final Logger LOG = Logger.getInstance(KarmaWatcher.class);
  private static final Pattern BASE_DIR_PATTERN = Pattern.compile("/[^/]*[*(].*$");

  private final StreamEventHandler myEventHandler = new StreamEventHandler() {
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

  private void startWatching(@NotNull final List<String> paths) {
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      @Override
      public void run() {
        for (String path : paths) {
          doWatch(path);
        }
      }
    });
  }

  private void doWatch(@NotNull String path) {
    LOG.info("Starting watching " + path);
    final LocalFileSystem fileSystem = LocalFileSystem.getInstance();
    String vfsPath = path.replace(File.separatorChar, '/');
    final VirtualFile file = fileSystem.findFileByPath(vfsPath);
    if (file != null && file.isValid()) {
      watchDirectory(fileSystem, file);
    }
    else {
      String baseDirPath = BASE_DIR_PATTERN.matcher(vfsPath).replaceFirst("");
      if (baseDirPath.isEmpty()) {
        baseDirPath = "/";
      }
      final VirtualFile baseDir = fileSystem.findFileByPath(vfsPath);
      if (baseDir != null) {
        watchDirectory(fileSystem, baseDir);
      }
    }

  }

  private void watchDirectory(@NotNull LocalFileSystem fileSystem, @NotNull VirtualFile dir) {
    VfsUtilCore.visitChildrenRecursively(dir, new VirtualFileVisitor() {
      @Override
      public boolean visitFile(@NotNull VirtualFile file) {
        file.getChildren();
        return true;
      }
    });
    LocalFileSystem.WatchRequest watchRequest = fileSystem.addRootToWatch(dir.getPath(), true);
    if (watchRequest == null) {
      LOG.warn("Can not watch " + dir.getPath());
    }
  }

  @NotNull
  public StreamEventHandler getEventHandler() {
    return myEventHandler;
  }
}
