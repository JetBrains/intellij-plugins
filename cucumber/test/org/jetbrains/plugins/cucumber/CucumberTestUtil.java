// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber;

import com.intellij.openapi.application.PathManager;

public final class CucumberTestUtil {
  public static String getTestDataPath() {
    return getPluginPath() + getShortTestPath();
  }

  public static String getPluginPath() {
    return PathManager.getHomePath() + getShortPluginPath();
  }

  public static String getShortPluginPath() {
    return "/contrib/cucumber";
  }

  public static String getShortTestPath() {
    return "/testData";
  }
}
