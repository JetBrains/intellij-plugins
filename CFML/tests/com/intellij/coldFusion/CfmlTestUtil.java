package com.intellij.coldFusion;

import com.intellij.openapi.application.PathManager;

import java.io.File;

/**
 * Created by fedorkorotkov.
 */
public class CfmlTestUtil {
  /**
   * Run from ultimate sources or like a separate plugin project(to contribute)
   */

  public static final boolean isInternalRun = !(new File("testData").exists());

  /**
   * The root of the test data directory
   */
  public static final String BASE_TEST_DATA_PATH = findTestDataPath();

  private static String findTestDataPath() {
    File f = new File("tests", "testData");
    if (f.exists()) {
      return f.getAbsolutePath();
    }
    return PathManager.getHomePath() + "/contrib/CFML/tests/testData";
  }
}
