package com.intellij.flex.uiDesigner;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public final class DebugPathManager {
  private static final String FLEX_TOOLS_FLEX_UI_DESIGNER = "/flex/tools/flex-ui-designer";

  public final static boolean IS_DEV;

  private static String ideaHome;
  private final static String fudHome;

  private DebugPathManager() {
  }

  static {
    ideaHome = PathManager.getHomePathFor(DebugPathManager.class);
    if (ideaHome == null) {
      ideaHome = PathManager.getHomePath();
    }

    Application app = ApplicationManager.getApplication();
    if (app == null) {
      // running ComplementSwfBuilder
      assert ideaHome != null;
      fudHome = ideaHome + FLEX_TOOLS_FLEX_UI_DESIGNER;
      IS_DEV = true; // not used actually
    }
    else if (app.isUnitTestMode()) {
      // running tests
      IS_DEV = true;
      assert ideaHome != null;
      fudHome = ideaHome + FLEX_TOOLS_FLEX_UI_DESIGNER;
    }
    else {
      fudHome = System.getProperty("fud.home");
      IS_DEV = fudHome != null;
      if (ideaHome != null && !IS_DEV) {
        ideaHome = null; // not used
      }
    }
  }

  public static String getIdeaHome() {
    return ideaHome;
  }

  public static String getFudHome() {
    return fudHome;
  }

  public static String getTestDataPath() {
    return fudHome + "/idea-plugin/testData";
  }

  public static String resolveTestArtifactPath(String path) {
    return resolveTestArtifactPath(path, null);
  }

  public static String resolveTestArtifactPath(String path, @Nullable String mavenPath) {
    boolean isTestArtifact = mavenPath == null;
    if (isTestArtifact) {
      String mavenSubFolder;
      if (path.equals("test-data-helper.swc")) {
        mavenSubFolder = "test-data-helper";
      }
      else {
        mavenSubFolder = "test-plugin";
      }
      mavenPath = mavenSubFolder + "/target/" + path;
    }

    File file = new File(getFudHome(), mavenPath);
    if (!file.exists()) {
      String parent;
      if (isTestArtifact) {
        parent = getIdeaHome() + "/out/flex-ui-designer";
      }
      else {
        parent = PathManager.getResourceRoot(DebugPathManager.class, "/" + DebugPathManager.class.getName().replace('.', '/') + ".class");
      }
      file = new File(parent, path);
      if (!file.exists()) {
        throw new IllegalStateException("Cannot find " + file.getPath());
      }
    }
    return file.getPath();
  }
}
