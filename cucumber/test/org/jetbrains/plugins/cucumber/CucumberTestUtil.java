package org.jetbrains.plugins.cucumber;

import com.intellij.openapi.application.PathManager;

/**
 * User: Andrey.Vokin
 * Date: 1/10/13
 */
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
    return "/testdata";
  }
}
