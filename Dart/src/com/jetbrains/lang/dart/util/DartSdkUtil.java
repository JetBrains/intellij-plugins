package com.jetbrains.lang.dart.util;

import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.roots.JavadocOrderRootType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.jetbrains.lang.dart.ide.DartSdkData;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

/**
 * @author: Fedor.Korotkov
 */
public class DartSdkUtil {
  @Nullable
  public static DartSdkData testDartSdk(String path) {
    final String exePath = getCompilerPathByFolderPath(path);

    if (exePath == null) {
      return null;
    }
    return new DartSdkData(path, getSdkVersion(path));
  }

  public static String getSdkVersion(String path) {
    try {
      return FileUtil.loadFile(new File(path, "revision")).trim();
    }
    catch (IOException e) {
      return "NA";
    }
  }

  @Nullable
  public static String getCompilerPathByFolderPath(@Nullable String folderPath) {
    return getExecutablePathByFolderPath(folderPath, "dart");
  }

  @Nullable
  private static String getExecutablePathByFolderPath(@Nullable String folderPath, String name) {
    if (folderPath == null) {
      return null;
    }
    final String folderUrl = VfsUtilCore.pathToUrl(folderPath);
    final String candidate = folderUrl + "/bin/" + getExecutableName(name);
    if (fileExists(candidate)) {
      return FileUtil.toSystemIndependentName(VfsUtilCore.urlToPath(candidate));
    }

    return null;
  }

  private static String getExecutableName(String name) {
    if (SystemInfo.isWindows) {
      return name + ".exe";
    }
    return name;
  }

  private static boolean fileExists(@Nullable String filePath) {
    return filePath != null && checkFileExists(VirtualFileManager.getInstance().findFileByUrl(filePath));
  }

  private static boolean checkFileExists(@Nullable VirtualFile file) {
    return file != null && file.exists();
  }

  public static void setupSdkPaths(@Nullable VirtualFile sdkRoot, SdkModificator modificator) {
    if (sdkRoot == null) {
      return;
    }
    final VirtualFile libRoot = sdkRoot.findChild("lib");
    if (libRoot != null) {
      for (VirtualFile child : libRoot.getChildren()) {
        if (!"html".equals(child.getName()) && !"_internal".equals(child.getName())) {
          modificator.addRoot(child, OrderRootType.SOURCES);
          modificator.addRoot(child, OrderRootType.CLASSES);
        }
      }
    }

    final VirtualFile docRoot = sdkRoot.findChild("doc");
    if (docRoot != null) {
      modificator.addRoot(docRoot, JavadocOrderRootType.getInstance());
    }
  }

  public static int findFreePortForDebugging() {
    ServerSocket socket = null;
    try {
      socket = new ServerSocket(0);
      return socket.getLocalPort();
    }
    catch (IOException e) {
    }
    finally {
      if (socket != null) {
        try {
          socket.close();
        }
        catch (IOException ignored) {
        }
      }
    }
    return -1;
  }
}
