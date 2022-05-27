package com.jetbrains.plugins.meteor.tsStubs;

import com.intellij.openapi.application.PathManager;

public final class MeteorTestUtil {

  public static String getTestDataPath() {
    return getPluginPath() + getShortTestPath();
  }

  public static String getPluginPath() {
    return PathManager.getHomePath() + getShortPluginPath();
  }

  public static String getShortPluginPath() {
    return "/contrib/Meteor";
  }

  public static String getShortTestPath() {
    return "/testData";
  }

  public static String getBasePath() {
    return getShortPluginPath() + getShortTestPath();
  }

  public static void enableMeteor() {
    System.setProperty("meteor.js", "enable");
  }

  public static void disableMeteor() {
    System.setProperty("meteor.js", "disable");
  }
}
