// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs;

import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy;

import java.io.File;

public final class PrettierJSTestUtil {

  private PrettierJSTestUtil() {
  }

  public static String getTestDataPath() {
    return getContribPath() + "/prettierJS/test/data/";
  }


  private static String getContribPath() {
    final String homePath = IdeaTestExecutionPolicy.getHomePathWithPolicy();
    if (new File(homePath, "contrib/.gitignore").isFile()) {
      return homePath + File.separatorChar + "contrib";
    }
    return homePath;
  }
}
