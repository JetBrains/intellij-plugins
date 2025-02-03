// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion;

import com.intellij.openapi.application.PathManager;

import java.io.File;

/**
 * Created by fedorkorotkov.
 */
public final class CfmlTestUtil {
  /**
   * The root of the test data directory
   */
  public static final String BASE_TEST_DATA_PATH = findTestDataPath();

  private static String findTestDataPath() {
    File absoluteTestData = new File(PathManager.getHomePath(), "contrib/CFML/testData");
    if (absoluteTestData.exists()) {
      return absoluteTestData.getAbsolutePath();
    }

    File f = new File("testData");
    if (f.exists()) {
      return f.getAbsolutePath();
    }

    throw new IllegalStateException("Unable to find testData directory");
  }
}
