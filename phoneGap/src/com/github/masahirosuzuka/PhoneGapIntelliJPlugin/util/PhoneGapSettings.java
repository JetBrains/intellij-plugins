package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.util;

import com.intellij.openapi.externalSystem.model.ProjectSystemId;

/**
 * Created by Masahiro Suzuka on 2014/04/12.
 */

// Store PhoneGapSettings class
final public class PhoneGapSettings {

  // System ID
  public static final ProjectSystemId PHONEGAP_SYSTEM_ID = new ProjectSystemId("PHONEGAP");
  public static final ProjectSystemId CORDOVA_SYSTEM_ID = new ProjectSystemId("CORDOVA");

  // External tools PATH
  public static String NODEJS_PATH = "/usr/local/bin/node";
  public static String ANDROID_SDK = "android";
  public static String IOS_SIM = "ios-sim";

  // PhoneGap PATH
  public static String PHONEGAP_PATH = "/usr/local/bin/phonegap";

  // PhoneGap commands
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
  public static boolean isPhoneGapInstallded = false;

  private static PhoneGapSettings instance;

  private PhoneGapSettings() {}

  public static PhoneGapSettings getInstance() {
    if (instance == null) {
      instance = new PhoneGapSettings();
    }
    return instance;
  }

  public static boolean isIsPhoneGapInstallded() {
    return isPhoneGapInstallded;
  }

  public static void setIsPhoneGapInstallded(boolean isPhoneGapInstallded) {
    PhoneGapSettings.isPhoneGapInstallded = isPhoneGapInstallded;
  }

}
