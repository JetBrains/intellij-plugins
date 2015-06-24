package com.intellij.javascript.karma.util;

import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.javascript.nodejs.CompletionModuleInfo;
import com.intellij.javascript.nodejs.NodeModuleSearchUtil;
import com.intellij.javascript.nodejs.NodePathSettings;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.content.Content;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.io.LocalFileFinder;

import java.io.File;
import java.util.List;

public class KarmaUtil {

  public static final String NODE_PACKAGE_NAME = "karma";
  private static final String[] FILE_NAME_SUFFIXES = new String[] {".conf.js", "-conf.js", ".config.js", "-config.js", "karma.conf.coffee"};

  private KarmaUtil() {
  }

  public static void selectAndFocusIfNotDisposed(@NotNull RunnerLayoutUi ui,
                                                 @NotNull Content content,
                                                 boolean requestFocus,
                                                 boolean forced) {
    if (!ui.isDisposed()) {
      ui.selectAndFocus(content, requestFocus, forced);
    }
  }

  public static boolean isKarmaConfigFile(@NotNull CharSequence filename) {
    for (String suffix : FILE_NAME_SUFFIXES) {
      if (StringUtil.endsWith(filename, suffix)) {
        return true;
      }
    }
    return false;
  }

  @Nullable
  public static VirtualFile getRequester(@NotNull Project project, @NotNull String configFilePath) {
    VirtualFile requester = null;
    if (StringUtil.isNotEmpty(configFilePath)) {
      File configFile = new File(configFilePath);
      if (configFile.isFile()) {
        requester = VfsUtil.findFileByIoFile(configFile, false);
      }
    }
    if (requester == null || !requester.isValid()) {
      requester = project.getBaseDir();
    }
    return requester;
  }

  public static boolean isPathUnderContentRoots(@NotNull Project project, @NotNull String filePath) {
    VirtualFile file = LocalFileFinder.findFile(FileUtil.toSystemIndependentName(filePath));
    if (file == null || !file.isValid()) {
      return false;
    }
    VirtualFile contentRoot = ProjectFileIndex.SERVICE.getInstance(project).getContentRootForFile(file, false);
    return contentRoot != null;
  }

  @Nullable
  public static String detectKarmaPackageDir(@NotNull Project project,
                                             @NotNull String configFilePath,
                                             @NotNull String nodeInterpreterPath) {
    List<CompletionModuleInfo> modules = ContainerUtil.newArrayList();
    VirtualFile requester = getRequester(project, configFilePath);
    NodePathSettings nodeSettings = StringUtil.isEmptyOrSpaces(nodeInterpreterPath) ? null : new NodePathSettings(nodeInterpreterPath);
    NodeModuleSearchUtil.findModulesWithName(modules,
                                             NODE_PACKAGE_NAME,
                                             requester,
                                             nodeSettings,
                                             true);
    for (CompletionModuleInfo module : modules) {
      VirtualFile moduleRoot = module.getVirtualFile();
      if (moduleRoot != null && moduleRoot.isValid() && moduleRoot.isDirectory()) {
        return FileUtil.toSystemDependentName(moduleRoot.getPath());
      }
    }
    return null;
  }
}
