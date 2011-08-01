package com.intellij.flex.uiDesigner;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public final class DebugPathManager {
  private static final String FLEX_TOOLS_FLEX_UI_DESIGNER = "/flex/tools/flex-ui-designer";

  public final static boolean IS_DEV;
  public final static String ADL_EXECUTABLE;
  public final static String ADL_RUNTIME;

  private static String ideaHome;
  private static String fudHome;

  static {
    Application app = ApplicationManager.getApplication();
    if (app == null) {
      IS_DEV = true; // e.g. SwcDependenciesSorter.main() is running
    }
    else if (app.isUnitTestMode()) {
      IS_DEV = true;
    }
    else {
      IS_DEV = !"jar".equals(DebugPathManager.class.getResource("").getProtocol());
    }

    if (app != null && app.isUnitTestMode()) {
      // classpath contains classes from the IDEA project
      ideaHome = getRootByClass(DebugPathManager.class);
      fudHome = ideaHome + FLEX_TOOLS_FLEX_UI_DESIGNER;
    }
    else {
      // classpath contains classes from plugins sandbox
      fudHome = System.getProperty("fud.home");
      if (fudHome == null) {
        throw new IllegalStateException("Please define 'fud.home' to point to 'IDEA" + FLEX_TOOLS_FLEX_UI_DESIGNER + "' folder");
      }
      ideaHome = null; // we need it only in tests
    }

    String adlExecutable = System.getProperty("adl.executable");
    if (adlExecutable == null) {
      if (SystemInfo.isMac) {
        adlExecutable = "/Developer/SDKs/flex_4.5.1/bin/adl";
      }
      else {
        throw new IllegalStateException("Please define 'adl.executable' to point to ADL executable");
      }
    }
    ADL_EXECUTABLE = adlExecutable;

    String adlRuntime = System.getProperty("adl.runtime");
    if (adlRuntime == null) {
      if (SystemInfo.isMac) {
        adlRuntime = "/Library/Frameworks";
      }
      else {
        throw new IllegalStateException("Please define 'adl.runtime' to point to ADL runtime");
      }
    }
    ADL_RUNTIME = adlRuntime;
  }

  public static String getIdeaHome() {
    return ideaHome;
  }

  public static String getFudHome() {
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
