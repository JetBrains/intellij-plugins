package com.jetbrains.lang.dart.util;

import com.intellij.openapi.application.PathManager;

import java.io.File;

public class DartTestUtils {

  public static final String BASE_TEST_DATA_PATH = findTestDataPath();

  private static String findTestDataPath() {
    return PathManager.getHomePath() + "/contrib/Dart/testData";
  }
}
