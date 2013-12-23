package com.intellij.javascript.karma.server.watch;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.regex.Pattern;

public class KarmaWatchPattern {

  private static final Logger LOG = Logger.getInstance(KarmaWatchPattern.class);
  private static final Pattern[] BASE_DIR_PATTERNS = new Pattern[] {
    // /path/to/{,model/}*.js',
    Pattern.compile("/[^/]*\\{.*\\}.*$"),

    // /path/to/*.js
    Pattern.compile("/[^/]*\\*.*$"),

    // /path/to/!(...)
    Pattern.compile("/[^/]*!\\(.*$"),

    // /path/to/+(...)
    Pattern.compile("/[^/]*\\+\\(.*$"),

    // /path/to/(...)?
    Pattern.compile("/[^/]*\\)\\?.*$"),
  };

  private final LocalFileSystem myFileSystem;
  private final KarmaChangedFilesManager myChangedFileManager;
  private final String myVfsPath;
  private final String myBasePathDir;
  private final boolean myCheckBasePathDir;
  private LocalFileSystem.WatchRequest myWatchRequest;
  private VirtualFile myRoot;
  private String myRootPath;

  KarmaWatchPattern(@NotNull LocalFileSystem fileSystem,
                    @NotNull KarmaChangedFilesManager changedFilesManager,
                    @NotNull final String pattern) {
    LOG.info("Start watching path pattern " + pattern);

    myFileSystem = fileSystem;
    myChangedFileManager = changedFilesManager;
    myVfsPath = pattern.replace(File.separatorChar, KarmaWatchSession.SEPARATOR_CHAR);
    String baseDirPath = extractBaseDir(myVfsPath);
    if (baseDirPath.isEmpty()) {
      baseDirPath = KarmaWatchSession.SEPARATOR;
    }
    myBasePathDir = baseDirPath;
    myCheckBasePathDir = !myVfsPath.equals(myBasePathDir);

    update(false);
  }

  @NotNull
  public static String extractBaseDir(@NotNull String filePattern) {
    for (Pattern pattern : BASE_DIR_PATTERNS) {
      filePattern = pattern.matcher(filePattern).replaceFirst("");
    }
    return filePattern;
  }

  public void update(boolean rescan) {
    boolean noRootBefore = false;
    boolean rootValid = myRoot != null && myRoot.isValid();
    if (rootValid) {
      String path = myRoot.getPath();
      if (!path.equals(myRootPath)) {
        rootValid = false;
        if (myRootPath != null) {
          VfsUtilCore.visitChildrenRecursively(myRoot, new VirtualFileVisitor() {
            @Override
            public boolean visitFile(@NotNull VirtualFile file) {
              if (!file.isDirectory()) {
                String subPath = VfsUtilCore.getRelativePath(file, myRoot, KarmaWatchSession.SEPARATOR_CHAR);
                if (subPath != null) {
                  myChangedFileManager.onFileRemoved(KarmaWatchSession.join(myRootPath, subPath));
                }
              }
              return true;
            }
          });
        }
      }
    }
    if (!rootValid) {
      noRootBefore = true;
      myRoot = null;
      myRootPath = null;
      stopWatching();
      final VirtualFile file = myFileSystem.findFileByPath(myVfsPath);
      if (file != null && file.isValid()) {
        myRoot = file;
      }
      else if (myCheckBasePathDir) {
        final VirtualFile baseDir = myFileSystem.findFileByPath(myBasePathDir);
        if (baseDir != null && baseDir.isValid()) {
          myRoot = baseDir;
        }
      }
      if (myRoot != null) {
        myRootPath = myRoot.getPath();
      }
    }
    if (myRoot == null) {
      LOG.warn("[Karma watch] Can not find vfs root for " + myBasePathDir);
      return;
    }
    if (myWatchRequest == null) {
      myWatchRequest = watchRoot(myRoot, rescan && noRootBefore);
      LOG.info("Watching " + myRoot.getPath());
    }
  }

  @Nullable
  private LocalFileSystem.WatchRequest watchRoot(@NotNull final VirtualFile dir, final boolean reportChildren) {
    VfsUtilCore.visitChildrenRecursively(dir, new VirtualFileVisitor() {
      @Override
      public boolean visitFile(@NotNull VirtualFile file) {
        if (reportChildren && !file.isDirectory()) {
          myChangedFileManager.onFileAdded(file.getPath());
        }
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
    myWatchRequest = null;
  }

  @Nullable
  public String findWatchedOriginalPath(@NotNull VirtualFile file) {
    VirtualFile root = myRoot;
    if (root != null && VfsUtilCore.isAncestor(root, file, false)) {
      String filePath = file.getPath();
      if (SystemInfo.isWindows) {
        String originalRoot = myCheckBasePathDir ? myBasePathDir : myVfsPath;
        if (StringUtil.startsWithIgnoreCase(filePath, originalRoot)) {
          return originalRoot + filePath.substring(originalRoot.length());
        }
      }
      return filePath;
    }
    return null;
  }
}
