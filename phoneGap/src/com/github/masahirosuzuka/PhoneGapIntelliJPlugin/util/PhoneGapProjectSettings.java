package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.util;

/**
 * PhoneGapProjectSettings.java
 *
 * Managing project local setting.
 *  phonegap-plugin
 *  plugman
 *  Grunt
 *
 * Created by Masahiro Suzuka on 2014/05/30.
 */
public class PhoneGapProjectSettings {

  private PhoneGapProjectSettings INSTANCE = new PhoneGapProjectSettings();

  private PhoneGapProjectSettings() {}

  public PhoneGapProjectSettings getInstance() {
    return INSTANCE;
  }
}
