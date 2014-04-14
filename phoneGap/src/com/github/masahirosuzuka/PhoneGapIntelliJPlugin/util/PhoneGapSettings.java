package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.util;

/**
 * Created by Masahiro Suzuka on 2014/04/12.
 */

// Store PhoneGapSettings class
final public class PhoneGapSettings {

  public static String PHONEGAP_COMMAND = "phonegap";
  public static String PHONEGAP_TASK = "run";
  public static String PHONEGAP_PLATFORM_ANDROID = "android";
  public static String PHONEGAP_PLATFORM_IOS = "ios";
  public static String PHONEGAP_RELEASEBUILD = "--release";

  public static String PHONEGAP_FOLDERS_CORDOVA = ".cordova";
  public static String PHONEGAP_FOLDERS_HOOKS = "hooks";
  public static String PHONEGAP_FOLDERS_MERGES = "merges";
  public static String PHONEGAP_FOLDERS_NODE_MODULES = "node_modules";
  public static String PHONEGAP_FOLDERS_PLATFORMS = "platforms";
  public static String PHONEGAP_FOLDERS_PLUGINS = "plugins";
  public static String PHONEGAP_FOLDERS_WWW = "www";

  private static PhoneGapSettings instance;

  private PhoneGapSettings() {}

  public static PhoneGapSettings getInstance() {
    if (instance == null) {
      instance = new PhoneGapSettings();
    }
    return instance;
  }
}
