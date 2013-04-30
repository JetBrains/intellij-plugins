package com.google.jstestdriver.idea;

import com.intellij.openapi.application.PathManager;

import java.io.File;

/**
 * @author Sergey Simonchik
 */
public class JstdTestRoot {
  private JstdTestRoot() {}

  public static File getTestDataDir() {
    return new File(PathManager.getHomePath(), "contrib/JsTestDriver/test/testData/");
  }

}
