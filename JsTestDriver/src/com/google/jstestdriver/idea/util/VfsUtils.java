package com.google.jstestdriver.idea.util;

import com.google.common.collect.Lists;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.List;

public class VfsUtils {

  private VfsUtils() {}

  public static List<VirtualFile> findVirtualFilesByResourceNames(final Class<?> markerClass, final String[] resourceNames) {
    return ApplicationManager.getApplication().runReadAction(new Computable<List<VirtualFile>>() {
      @Override
      public List<VirtualFile> compute() {
        List<VirtualFile> virtualFiles = Lists.newArrayList();
        for (String resourceName : resourceNames) {
          VirtualFile virtualFile = findVirtualFile(markerClass, resourceName);
          virtualFiles.add(virtualFile);
        }
        return virtualFiles;
      }
    });
  }

  private static VirtualFile findVirtualFile(Class<?> markerClazz, String resourceName) {
    VirtualFile file = VfsUtil.findFileByURL(markerClazz.getResource(resourceName));
    if (file == null) {
      throw new RuntimeException("Can't find virtual file for '" + resourceName + "', class " + markerClazz);
    }
    return file;
  }

}
