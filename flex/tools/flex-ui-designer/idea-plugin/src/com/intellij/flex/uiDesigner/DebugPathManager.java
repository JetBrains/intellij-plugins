package com.intellij.flex.uiDesigner;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;

public final class DebugPathManager {
  private static final String FLEX_TOOLS_FLEX_UI_DESIGNER = "/flex/tools/flex-ui-designer";

  public final static boolean IS_DEV;

  private static String ideaHome;
  private final static String fudHome;

  private DebugPathManager() {
  }

  static {
    ideaHome = PathManager.getHomePathFor(DebugPathManager.class);
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
}
