package com.intellij.flex.uiDesigner;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public final class DebugPathManager {
  private static String ideaHome;
  private static String fudHome;
  
  public final static boolean IS_DEV = System.getProperty("fud.dev") != null || isUnitTestMode();
  private static final String FLEX_TOOLS_FLEX_UI_DESIGNER = "/flex/tools/flex-ui-designer";

  private static boolean isUnitTestMode() {
    Application app = ApplicationManager.getApplication();
    return app == null || app.isUnitTestMode();
  }

  public static String getIdeaHome() {
    getFudHome();
    return ideaHome;
  }

  public static String getFudHome() {
    if (fudHome == null) {
      if (isUnitTestMode()) {
        ideaHome = getRootByClass(PathManager.class);
        if (ideaHome == null) {
          ideaHome = System.getProperty("user.dir");
          ideaHome = ideaHome.substring(0, ideaHome.length() - FLEX_TOOLS_FLEX_UI_DESIGNER.length());
        }
        fudHome = ideaHome + FLEX_TOOLS_FLEX_UI_DESIGNER;
      }
      else {
        fudHome = System.getProperty("fud.home");
        assert fudHome != null;
      }
    }

    return fudHome;
  }

  @Nullable
  private static String getRootByClass(Class aClass) {
    String rootPath = PathManager.getResourceRoot(aClass, "/" + aClass.getName().replace('.', '/') + ".class");
    File root = new File(rootPath).getAbsoluteFile();
    do {
      final String parent = root.getParent();
      if (parent == null) {
        return null;
      }
      root = new File(parent).getAbsoluteFile();
    }
    while (root != null && !isIdeaHome(root));

    return root == null ? null : root.getAbsolutePath();
  }

  private static boolean isIdeaHome(final File root) {
    return new File(root, FileUtil.toSystemDependentName("bin/idea.properties")).exists() ||
           new File(root, FileUtil.toSystemDependentName("community/bin/idea.properties")).exists();
  }
}
