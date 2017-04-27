package com.google.jstestdriver.idea.util;

import com.google.common.collect.Lists;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VfsUtils {

  private VfsUtils() {}

  @NotNull
  public static List<VirtualFile> findVirtualFilesByResourceNames(final Class<?> markerClass, final String[] resourceNames) {
    return ReadAction.compute(() -> {
      List<VirtualFile> virtualFiles = Lists.newArrayList();
      for (String resourceName : resourceNames) {
        VirtualFile virtualFile = findVirtualFileByResourceName(markerClass, resourceName);
        virtualFiles.add(virtualFile);
      }
      return virtualFiles;
    });
  }

  @NotNull
  private static VirtualFile findVirtualFileByResourceName(Class<?> markerClazz, String resourceName) {
    VirtualFile file = VfsUtil.findFileByURL(markerClazz.getResource(resourceName));
    if (file == null) {
      throw new RuntimeException("Can't find virtual file for '" + resourceName + "', class " + markerClazz);
    }
    return file;
  }

}
