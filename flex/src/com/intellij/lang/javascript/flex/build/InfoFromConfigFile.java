package com.intellij.lang.javascript.flex.build;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.Nullable;

public class InfoFromConfigFile {
  public static InfoFromConfigFile DEFAULT = new InfoFromConfigFile(null, null, null, null, null);

  private final @Nullable VirtualFile myConfigFile;
  private final @Nullable String myMainClassPath;
  private boolean myMainClassInitialized = false;
  private @Nullable String myMainClass;
  private final @Nullable String myOutputFileName;
  private final @Nullable String myOutputFolderPath;
  private final @Nullable String myTargetPlayer;

  InfoFromConfigFile(final @Nullable VirtualFile configFile,
                     final @Nullable String mainClassPath,
                     final @Nullable String outputFileName,
                     final @Nullable String outputFolderPath,
                     final @Nullable String targetPlayer) {
    myConfigFile = configFile;
    myMainClassPath = mainClassPath;
    myOutputFileName = outputFileName;
    myOutputFolderPath = outputFolderPath;
    myTargetPlayer = targetPlayer;
  }

  @Nullable
  public String getMainClass(final Module module) {
    if (!myMainClassInitialized && myConfigFile != null && myConfigFile.isValid()) {
      myMainClass = myMainClassPath == null ? null : getClassForOutputTagValue(module, myMainClassPath, myConfigFile.getParent());
    }
    myMainClassInitialized = true;
    return myMainClass;
  }

  @Nullable
  public String getOutputFileName() {
    return myOutputFileName;
  }

  @Nullable
  public String getOutputFolderPath() {
    return myOutputFolderPath;
  }

  @Nullable
  public String getTargetPlayer() {
    return myTargetPlayer;
  }

  private static String getClassForOutputTagValue(final Module module, final String outputTagValue, final VirtualFile baseDir) {
    if (outputTagValue.isEmpty()) return "unknown";

    final VirtualFile file = VfsUtil.findRelativeFile(outputTagValue, baseDir);
    if (file == null) return FileUtil.getNameWithoutExtension(PathUtil.getFileName(outputTagValue));

    final VirtualFile sourceRoot = ProjectRootManager.getInstance(module.getProject()).getFileIndex().getSourceRootForFile(file);
    if (sourceRoot == null) return file.getNameWithoutExtension();

    final String relativePath = VfsUtilCore.getRelativePath(file, sourceRoot, '/');
    return relativePath == null ? file.getNameWithoutExtension() : FileUtil.getNameWithoutExtension(relativePath).replace("/", ".");
  }
}
