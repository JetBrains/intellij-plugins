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
    ideaHome = PathManager.getHomePathFor(DebugPathManager.class);
    Application app = ApplicationManager.getApplication();
    if (app == null) {
      // running SwcDependenciesSorter
      assert ideaHome != null;
      fudHome = ideaHome + FLEX_TOOLS_FLEX_UI_DESIGNER;
      IS_DEV = true; // not used actually
      ADL_EXECUTABLE = null; // not used
      ADL_RUNTIME = null; // not used
    }
    else if (app.isUnitTestMode()) {
      // running tests
      IS_DEV = true;
      assert ideaHome != null;
      fudHome = ideaHome + FLEX_TOOLS_FLEX_UI_DESIGNER;

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
    else if (ideaHome != null) {
      // running bundled product, or from IDEA project
      IS_DEV = false;
      ADL_EXECUTABLE = null; // not used
      ADL_RUNTIME = null; // not used
      ideaHome = null; // not used
      fudHome = null; // not used
    }
    else {
      // running from flex-ui-designer plugin project
      IS_DEV = true;
      fudHome = System.getProperty("fud.home");
      if (fudHome == null) {
        throw new IllegalStateException("Please define 'fud.home' to point to 'IDEA" + FLEX_TOOLS_FLEX_UI_DESIGNER + "' folder");
      }
      ADL_EXECUTABLE = null; // not used
      ADL_RUNTIME = null; // not used
    }
  }

  public static String getIdeaHome() {
    return ideaHome;
  }

  public static String getFudHome() {
    return fudHome;
  }

}
