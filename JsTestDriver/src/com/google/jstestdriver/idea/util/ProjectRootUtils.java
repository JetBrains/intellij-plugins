package com.google.jstestdriver.idea.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author Sergey Simonchik
 */
public class ProjectRootUtils {

  private ProjectRootUtils() {}

  public static boolean isInsideContentRoots(@NotNull Project project, @NotNull File file) {
    return getContentRootForFile(project, file) != null;
  }

  @Nullable
  private static VirtualFile getContentRootForFile(@NotNull Project project, @NotNull File file) {
    ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
    VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file);
    if (virtualFile != null) {
      return fileIndex.getContentRootForFile(virtualFile);
    }
    return null;
  }

  @Nullable
  public static String getRootRelativePath(@NotNull Project project, @NotNull String filePath) {
    VirtualFile contentRoot = getContentRootForFile(project, new File(filePath));
    VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(filePath);
    if (contentRoot == null || virtualFile == null) {
      return null;
    }
    if (contentRoot.equals(virtualFile)) {
      return contentRoot.getName();
    }
    return FileUtil.getRelativePath(contentRoot.getPath(), virtualFile.getPath(), '/');
  }
}
