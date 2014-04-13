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

  private static PhoneGapSettings instance;

  private PhoneGapSettings() {}

  public static PhoneGapSettings getInstance() {
    if (instance == null) {
      instance = new PhoneGapSettings();
    }
    return instance;
  }
}
