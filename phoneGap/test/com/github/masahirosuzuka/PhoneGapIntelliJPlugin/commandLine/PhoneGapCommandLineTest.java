package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine;


import com.intellij.testFramework.UsefulTestCase;

import java.util.List;

/**
 * Phonegap/Cordova old -> old output (version <3.5)
 */
public class PhoneGapCommandLineTest extends UsefulTestCase {

  public void testOldCordovaEmpty() {
    List<String> strings = PhoneGapCommandLine.parsePluginList("No plugins added. Use `cordova plugin add <plugin>`.");

    assertEmpty(strings);
  }

  public void testOldPhonegapEmpty() {
    List<String> strings = PhoneGapCommandLine.parsePluginList("[phonegap] no plugins installed");

    assertEmpty(strings);
  }

  public void testOldCordovaOne() {
   List<String> strings = PhoneGapCommandLine.parsePluginList("[ 'org.apache.cordova.console' ]");

    assertSameElements(strings, "org.apache.cordova.console");
  }

  public void testOldPhonegapOne() {
    List<String> strings = PhoneGapCommandLine.parsePluginList("[phonegap] org.apache.cordova.console");

    assertSameElements(strings, "org.apache.cordova.console");
  }

  public void testOldCordovaTwo1() {
    List<String> strings = PhoneGapCommandLine.parsePluginList("[ 'org.apache.cordova.console',\n" +
                                                               "  'org.chromium.polyfill.CustomEvent' ]");

    assertSameElements(strings, "org.apache.cordova.console", "org.chromium.polyfill.CustomEvent");
  }

  public void testOldCordovaTwo2() {
    List<String> strings = PhoneGapCommandLine.parsePluginList("[ 'org.apache.cordova.console', 'org.chromium.polyfill.CustomEvent' ]");

    assertSameElements(strings, "org.apache.cordova.console", "org.chromium.polyfill.CustomEvent");
  }

  public void testOldPhonegapTwo() {
    List<String> strings = PhoneGapCommandLine.parsePluginList("[phonegap] com.phonegap.plugins.mapkit\n" +
                                                               "[phonegap] org.apache.cordova.console");

    assertSameElements(strings, "org.apache.cordova.console", "com.phonegap.plugins.mapkit");
  }

  public void testNewPhonegapTwo() {
    List<String> strings = PhoneGapCommandLine.parsePluginList("""
                                                                 [phonegap] the following plugins are installed
                                                                 com.phonegap.plugins.mapkit 0.9.2 "MapKit"
                                                                 org.apache.cordova.console 0.2.9 "Console\"""");

    assertSameElements(strings, "com.phonegap.plugins.mapkit 0.9.2 \"MapKit\"", "org.apache.cordova.console 0.2.9 \"Console\"");
  }

  public void testNewPhonegapOne() {
    List<String> strings = PhoneGapCommandLine.parsePluginList("[phonegap] the following plugins are installed\n" +
                                                               "org.apache.cordova.console 0.2.9 \"Console\"");

    assertSameElements(strings, "org.apache.cordova.console 0.2.9 \"Console\"");
  }

  public void testNewPhonegapEmpty() {
    List<String> strings = PhoneGapCommandLine.parsePluginList("[phonegap] no plugins installed");

    assertEmpty(strings);
  }

  public void testNewCordovaEmpty() {
    List<String> strings = PhoneGapCommandLine.parsePluginList("No plugins added. Use `cordova plugin add <plugin>`.");

    assertEmpty(strings);
  }

  public void testNewCordovaOne() {
    List<String> strings = PhoneGapCommandLine.parsePluginList("org.apache.cordova.console 0.2.9 \"Console\"");

    assertSameElements(strings, "org.apache.cordova.console 0.2.9 \"Console\"");
  }

  public void testNewCordovaTwo() {
    List<String> strings = PhoneGapCommandLine.parsePluginList("com.phonegap.plugins.mapkit 0.9.2 \"MapKit\"\n" +
                                                               "org.apache.cordova.console 0.2.9 \"Console\"");

    assertSameElements(strings, "org.apache.cordova.console 0.2.9 \"Console\"", "com.phonegap.plugins.mapkit 0.9.2 \"MapKit\"");
  }

  public void testVersionOfPhonegapParser1() {
    assertTrue(PhoneGapExecutor.isPhoneGapAfter363("3.6.3"));
  }

  public void testVersionOfPhonegapParser2() {
    assertFalse(PhoneGapExecutor.isPhoneGapAfter363("3.5.4"));
  }

  public void testVersionOfPhonegapParser3() {
    assertTrue(PhoneGapExecutor.isPhoneGapAfter363("4"));
  }
}
