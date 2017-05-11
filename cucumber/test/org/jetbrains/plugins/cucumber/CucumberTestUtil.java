package org.jetbrains.plugins.cucumber;

import com.intellij.openapi.application.PathManager;

public class CucumberTestUtil {
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
