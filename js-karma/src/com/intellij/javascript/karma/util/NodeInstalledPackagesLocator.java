package com.intellij.javascript.karma.util;

import com.intellij.javascript.nodejs.NodeSettings;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class NodeInstalledPackagesLocator {

  private static final NodeInstalledPackagesLocator INSTANCE = new NodeInstalledPackagesLocator();
  private static final String[] FILE_EXTENSIONS = {".js", ".coffee", ".json", ".node"};

  @NotNull
  public static NodeInstalledPackagesLocator getInstance() {
    return INSTANCE;
  }

  @Nullable
  public NodeInstalledPackage findInstalledPackages(@NotNull String packageName,
                                                    @NotNull File currentDir,
                                                    @Nullable NodeSettings nodeSettings) {
    NodeInstalledPackage pkg = findPackageInNodeModulesDir(packageName, currentDir);
    if (pkg != null) {
      return pkg;
    }
    if (nodeSettings != null) {
      pkg = findPackageInGlobalFolders(packageName, nodeSettings);
      if (pkg != null) {
        return pkg;
      }
    }
    return null;
  }

  @Nullable
  private static NodeInstalledPackage findPackageInGlobalFolders(@NotNull String packageName,
                                                                 @NotNull NodeSettings nodeSettings) {
    List<VirtualFile> globalFolders = nodeSettings.getGlobalFolders();
    for (VirtualFile dir : globalFolders) {
      if (dir.isValid() && dir.isDirectory()) {
        File ioDir = new File(dir.getPath());
        NodeInstalledPackage pkg = findPackageInDirectory(packageName, ioDir);
        if (pkg != null) {
          return pkg;
        }
      }
    }
    return null;
  }

  @Nullable
  private static NodeInstalledPackage findPackageInNodeModulesDir(@NotNull String packageName,
                                                                  @NotNull File currentDir) {
    File dir = currentDir;
    while (dir != null) {
      File nodeModulesDir = new File(dir, "node_modules");
      if (nodeModulesDir.isDirectory()) {
        NodeInstalledPackage pkg = findPackageInDirectory(packageName, nodeModulesDir);
        if (pkg != null) {
          return pkg;
        }
      }
      dir = dir.getParentFile();
    }
    return null;
  }

  @Nullable
  private static NodeInstalledPackage findPackageInDirectory(@NotNull String packageName, @NotNull File dir) {
    File packageDir = new File(dir, packageName);
    if (packageDir.isDirectory() && canLoadDirectory(packageDir)) {
      return new NodeInstalledPackage(packageName, packageDir);
    }
    return null;
  }

  private static boolean canLoadDirectory(@NotNull File dir) {
    File packageJson = new File(dir, "package.json");
    if (packageJson.isFile()) {
      return true;
    }
    for (String ext : FILE_EXTENSIONS) {
      File file = new File(dir, "index" + ext);
      if (file.isFile()) {
        return true;
      }
    }
    return false;
  }

}
