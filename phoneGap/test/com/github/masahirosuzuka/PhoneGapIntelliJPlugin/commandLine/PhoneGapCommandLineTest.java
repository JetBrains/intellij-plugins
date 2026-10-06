package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine;


import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phonegap/Cordova old -> old output (version <3.5)
 */
public class PhoneGapCommandLineTest {

  @Test
  public void testOldCordovaEmpty() {
    List<String> strings = PhoneGapCommandLine.parsePluginList("No plugins added. Use `cordova plugin add <plugin>`.");

    assertThat(strings).isEmpty();
  }

  @Test
  public void testOldPhonegapEmpty() {
    List<String> strings = PhoneGapCommandLine.parsePluginList("[phonegap] no plugins installed");

    assertThat(strings).isEmpty();
  }

  @Test
  public void testOldCordovaOne() {
   List<String> strings = PhoneGapCommandLine.parsePluginList("[ 'org.apache.cordova.console' ]");

    assertThat(strings).containsExactlyInAnyOrder("org.apache.cordova.console");
  }

  @Test
  public void testOldPhonegapOne() {
    List<String> strings = PhoneGapCommandLine.parsePluginList("[phonegap] org.apache.cordova.console");

    assertThat(strings).containsExactlyInAnyOrder("org.apache.cordova.console");
  }

  @Test
  public void testOldCordovaTwo1() {
    List<String> strings = PhoneGapCommandLine.parsePluginList("[ 'org.apache.cordova.console',\n" +
                                                               "  'org.chromium.polyfill.CustomEvent' ]");

    assertThat(strings).containsExactlyInAnyOrder("org.apache.cordova.console", "org.chromium.polyfill.CustomEvent");
  }

  @Test
  public void testOldCordovaTwo2() {
    List<String> strings = PhoneGapCommandLine.parsePluginList("[ 'org.apache.cordova.console', 'org.chromium.polyfill.CustomEvent' ]");

    assertThat(strings).containsExactlyInAnyOrder("org.apache.cordova.console", "org.chromium.polyfill.CustomEvent");
  }

  @Test
  public void testOldPhonegapTwo() {
    List<String> strings = PhoneGapCommandLine.parsePluginList("[phonegap] com.phonegap.plugins.mapkit\n" +
                                                               "[phonegap] org.apache.cordova.console");

    assertThat(strings).containsExactlyInAnyOrder("org.apache.cordova.console", "com.phonegap.plugins.mapkit");
  }

  @Test
  public void testNewPhonegapTwo() {
    List<String> strings = PhoneGapCommandLine.parsePluginList("""
                                                                 [phonegap] the following plugins are installed
                                                                 com.phonegap.plugins.mapkit 0.9.2 "MapKit"
                                                                 org.apache.cordova.console 0.2.9 "Console\"""");

    assertThat(strings).containsExactlyInAnyOrder("com.phonegap.plugins.mapkit 0.9.2 \"MapKit\"", "org.apache.cordova.console 0.2.9 \"Console\"");
  }

  @Test
  public void testNewPhonegapOne() {
    List<String> strings = PhoneGapCommandLine.parsePluginList("[phonegap] the following plugins are installed\n" +
                                                               "org.apache.cordova.console 0.2.9 \"Console\"");

    assertThat(strings).containsExactlyInAnyOrder("org.apache.cordova.console 0.2.9 \"Console\"");
  }

  @Test
  public void testNewPhonegapEmpty() {
    List<String> strings = PhoneGapCommandLine.parsePluginList("[phonegap] no plugins installed");

    assertThat(strings).isEmpty();
  }

  @Test
  public void testNewCordovaEmpty() {
    List<String> strings = PhoneGapCommandLine.parsePluginList("No plugins added. Use `cordova plugin add <plugin>`.");

    assertThat(strings).isEmpty();
  }

  @Test
  public void testNewCordovaOne() {
    List<String> strings = PhoneGapCommandLine.parsePluginList("org.apache.cordova.console 0.2.9 \"Console\"");

    assertThat(strings).containsExactlyInAnyOrder("org.apache.cordova.console 0.2.9 \"Console\"");
  }

  @Test
  public void testNewCordovaTwo() {
    List<String> strings = PhoneGapCommandLine.parsePluginList("com.phonegap.plugins.mapkit 0.9.2 \"MapKit\"\n" +
                                                               "org.apache.cordova.console 0.2.9 \"Console\"");

    assertThat(strings).containsExactlyInAnyOrder("org.apache.cordova.console 0.2.9 \"Console\"", "com.phonegap.plugins.mapkit 0.9.2 \"MapKit\"");
  }

  @Test
  public void testVersionOfPhonegapParser1() {
    assertTrue(PhoneGapExecutor.isPhoneGapAfter363("3.6.3"));
  }

  @Test
  public void testVersionOfPhonegapParser2() {
    assertFalse(PhoneGapExecutor.isPhoneGapAfter363("3.5.4"));
  }

  @Test
  public void testVersionOfPhonegapParser3() {
    assertTrue(PhoneGapExecutor.isPhoneGapAfter363("4"));
  }
}
