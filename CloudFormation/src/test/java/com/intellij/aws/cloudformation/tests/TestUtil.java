package com.intellij.aws.cloudformation.tests;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;

import java.io.File;

public class TestUtil {
  public static String getTestDataPath(String relativePath) {
    return getTestDataFile(relativePath).getPath() + File.separator;
  }

  public static File getTestDataFile(String relativePath) {
    return new File(getTestDataRoot(), relativePath);
  }

  private static File getTestDataRoot() {
    return new File("testData").getAbsoluteFile();
  }

  public static String getTestDataPathRelativeToIdeaHome(String relativePath) {
    return FileUtil.getRelativePath(new File(PathManager.getHomePath()), new File(getTestDataRoot(), relativePath));
  }
}
