package com.intellij.aws.cloudformation.tests;

import java.io.File;

public class TestUtil {
  public static String getTestDataPath(String relativePath) {
    return System.getProperty("user.dir") + ("/testData" + relativePath).replace('/', File.separatorChar);
  }
}
