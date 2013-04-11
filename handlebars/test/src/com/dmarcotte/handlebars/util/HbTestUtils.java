package com.dmarcotte.handlebars.util;

import com.intellij.openapi.application.PathManager;

import java.io.File;

public class HbTestUtils {
  /**
   * The root of the test data directory
   */
  public static final String BASE_TEST_DATA_PATH = findTestDataPath();

  private static String findTestDataPath() {
    File f = new File("test", "data");
    if (f.exists()) {
      return f.getAbsolutePath();
    }
    return PathManager.getHomePath() + "/contrib/handlebars/test/data";
  }
}
