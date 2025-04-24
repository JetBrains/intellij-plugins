// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy;

import java.io.File;

public class JadeTestUtil {

  public static String getBaseTestDataPath() {
    String contribPath = getContribPath();
    return contribPath + "/jade/testData/";
  }

  public static String getContribPath() {
    File f = new File("testData");
    if (f.exists()) {
      return f.getAbsoluteFile().getParentFile().getParent();
    }
    final String homePath = IdeaTestExecutionPolicy.getHomePathWithPolicy();
    if (new File(homePath, "contrib/.gitignore").isFile()) {
      return homePath + File.separatorChar + "contrib";
    }
    return homePath;
  }

}
