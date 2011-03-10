package com.intellij.flex.uiDesigner;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

final class DebugPathManager {
  private static String ideaHome;
  private static String fudHome;
  
  public static String getIdeaHome() {
    getFudHome();
    return ideaHome;
  }
  
  static String getFudHome() {
    if (fudHome == null) {
      Application app = ApplicationManager.getApplication();
      if (app == null || app.isUnitTestMode()) {
        ideaHome = getRootByClass(PathManager.class);
        fudHome = ideaHome + "/plugins/flex/tools/flex-ui-designer";
      }
      else {
        fudHome = System.getProperty("fud.home");
        assert fudHome != null;
      }
    }
    
    return fudHome;
  }

  private static @NotNull String getRootByClass(Class aClass) {
    String rootPath = PathManager.getResourceRoot(aClass, "/" + aClass.getName().replace('.', '/') + ".class");
    File root = new File(rootPath).getAbsoluteFile();
    do {
      root = new File(root.getParent()).getAbsoluteFile();
    }
    while (root != null && !isIdeaHome(root));

    assert root != null;
    return root.getAbsolutePath();
  }

  private static boolean isIdeaHome(final File root) {
    return new File(root, FileUtil.toSystemDependentName("bin/idea.properties")).exists() ||
            new File(root, FileUtil.toSystemDependentName("community/bin/idea.properties")).exists();
  }
}
