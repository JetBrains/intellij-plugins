package com.jetbrains.lang.dart.util;

import com.intellij.openapi.application.PathManager;

import java.io.File;

/**
 * Created by fedorkorotkov.
 */
public class DartTestUtils {
  /**
   * Run from ultimate sources or like a separate plugin project(to contribute)
   */

  public static final boolean isInternalRun = !(new File("testData").exists());

  /**
   * The root of the test data directory
   */
  public static final String BASE_TEST_DATA_PATH = findTestDataPath();

  private static String findTestDataPath() {
    File f = new File("testData");
    if (f.exists()) {
      return f.getAbsolutePath();
    }
    return PathManager.getHomePath() + "/contrib/Dart/testData";
  }

  public static final String RELATIVE_TEST_DATA_PATH = findRelativeTestDataPath();

  private static String findRelativeTestDataPath() {
    if (!isInternalRun) {
      return "/testData";
    }
    return "/contrib/Dart/testData";
  }
}
