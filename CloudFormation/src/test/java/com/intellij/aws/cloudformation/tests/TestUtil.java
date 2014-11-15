package com.intellij.aws.cloudformation.tests;

import java.io.File;

public class TestUtil {
  public static String getTestDataPath(String relativePath) {
    return new File(getTestDataRoot(), relativePath).getPath() + File.separator;
  }

  private static File getTestDataRoot() {
    return new File(System.getProperty("user.dir"), "testData");
  }

  public static String getTestDataPathRelativeToIdeaHome(String relativePath) {
    return "../../../testData/" + relativePath;
  }
}
