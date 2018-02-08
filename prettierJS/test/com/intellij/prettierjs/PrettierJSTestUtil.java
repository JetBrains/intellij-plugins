package com.intellij.prettierjs;

import com.intellij.openapi.application.PathManager;

public class PrettierJSTestUtil {

  private PrettierJSTestUtil() {
  }

  public static String getTestDataPath() {
    return PathManager.getHomePath() + "/contrib/prettierJS/test/data/";
  }
}
