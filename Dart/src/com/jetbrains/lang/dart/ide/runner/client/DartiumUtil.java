package com.jetbrains.lang.dart.ide.runner.client;

import com.intellij.ide.browsers.BrowserFamily;
import com.intellij.ide.browsers.BrowserSpecificSettings;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.ide.browsers.WebBrowserManager;
import com.intellij.ide.browsers.chrome.ChromeSettings;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.sdk.DartSdkUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Map;
import java.util.UUID;

public class DartiumUtil {

  private static final String DART_FLAGS_ENV_VAR = "DART_FLAGS";
  public static final String CHECKED_MODE_OPTION = "--checked";
  private static final String ENABLE_ASYNC_OPTION = "--enable-async";

  private static final UUID DARTIUM_ID = UUID.fromString("BFEE1B69-A97D-4338-8BA4-25170ADCBAA6");
  private static final String DARTIUM_NAME = "Dartium";

  @Nullable
  public static WebBrowser getDartiumBrowser() {
    WebBrowser browser = WebBrowserManager.getInstance().findBrowserById(DARTIUM_ID.toString());
    if (browser == null) browser = WebBrowserManager.getInstance().findBrowserById(DARTIUM_NAME);
    return browser != null && browser.getFamily() == BrowserFamily.CHROME ? browser : null;
  }

  @Nullable
  public static String getDartiumPathForSdk(final @NotNull String sdkHomePath) {
    final File sdkDir = new File(sdkHomePath);
    if (!sdkDir.isDirectory()) return null;

    final File dartDir = sdkDir.getParentFile();
    final String relativePath = SystemInfo.isMac ? "chromium/Chromium.app"
                                                 : SystemInfo.isWindows ? "chromium/chrome.exe"
                                                                        : "chromium/chrome";

    final File dartiumPath = new File(dartDir, relativePath);
    return dartiumPath.exists() ? FileUtil.toSystemIndependentName(dartiumPath.getPath()) : null;
  }

  @NotNull
  public static WebBrowser ensureDartiumBrowserConfigured(final @Nullable String dartiumPath) {
    final WebBrowser browser = getDartiumBrowser();
    if (browser == null) {
      return WebBrowserManager.getInstance().addBrowser(DARTIUM_ID, BrowserFamily.CHROME, DARTIUM_NAME, dartiumPath, true,
                                                        BrowserFamily.CHROME.createBrowserSpecificSettings());
    }
    else {
      if (!Comparing.equal(dartiumPath, browser.getPath())) {
        WebBrowserManager.getInstance().setBrowserPath(browser, dartiumPath, true);
      }
      return browser;
    }
  }

  @Nullable
  public static String getErrorMessageIfWrongDartiumPath(final @NotNull String dartiumPath) {
    // Don't warn if Dartium is not configured.
    if (dartiumPath.isEmpty()) return null;

    final File file = new File(dartiumPath);
    if (SystemInfo.isMac && !file.isDirectory() || !SystemInfo.isMac && !file.isFile()) {
      return DartBundle.message("warning.invalid.dartium.path");
    }

    return null;
  }

  public static void applyDartiumSettings(final @NotNull String dartiumPathFromUI, final @NotNull ChromeSettings dartiumSettingsFromUI) {
    final WebBrowser dartiumInitial = getDartiumBrowser();
    final String dartiumPathInitial = dartiumInitial == null ? null : dartiumInitial.getPath();

    if (!dartiumPathFromUI.isEmpty() && new File(dartiumPathFromUI).exists() && !dartiumPathFromUI.equals(dartiumPathInitial)) {
      DartSdkUtil.updateKnownDartiumPaths(dartiumPathInitial, dartiumPathFromUI);

      final WebBrowser browser = ensureDartiumBrowserConfigured(dartiumPathFromUI);
      if (!dartiumSettingsFromUI.equals(browser.getSpecificSettings())) {
        WebBrowserManager.getInstance().setBrowserSpecificSettings(browser, dartiumSettingsFromUI);
      }
      return;
    }

    if (dartiumInitial != null && !dartiumSettingsFromUI.equals(dartiumInitial.getSpecificSettings())) {
      WebBrowserManager.getInstance().setBrowserSpecificSettings(dartiumInitial, dartiumSettingsFromUI);
    }
  }

  public static boolean isCheckedMode(@NotNull final Map<String, String> envVars) {
    return hasDartFlag(envVars, CHECKED_MODE_OPTION);
  }

  private static boolean hasDartFlag(final Map<String, String> envVars, final String dartFlag) {
    final String dartFlags = envVars.get(DART_FLAGS_ENV_VAR);
    return dartFlags != null && (dartFlags.trim().equals(dartFlag) ||
                                 dartFlags.startsWith(dartFlag + " ") ||
                                 dartFlags.endsWith(" " + dartFlag) ||
                                 dartFlags.contains(" " + dartFlag + " "));
  }

  public static void setCheckedMode(@NotNull final Map<String, String> envVars, final boolean checkedMode) {
    setDartFlagState(envVars, CHECKED_MODE_OPTION, checkedMode);
  }

  private static void setDartFlagState(final Map<String, String> envVars, final String dartFlag, final boolean flagState) {
    final boolean oldFlagState = hasDartFlag(envVars, dartFlag);
    if (oldFlagState == flagState) return;

    final String dartFlags = envVars.get(DART_FLAGS_ENV_VAR);
    if (flagState) {
      if (dartFlags == null) {
        envVars.put(DART_FLAGS_ENV_VAR, dartFlag);
      }
      else {
        envVars.put(DART_FLAGS_ENV_VAR, dartFlags + " " + dartFlag);
      }
    }
    else {
      String newFlags = dartFlags;
      if (newFlags.trim().equals(dartFlag)) {
        newFlags = "";
      }
      newFlags = StringUtil.trimStart(newFlags, dartFlag + " ");
      newFlags = StringUtil.trimEnd(newFlags, " " + dartFlag);
      final int index = newFlags.indexOf(" " + dartFlag + " ");
      if (index != -1) {
        // keep one space between parts
        newFlags = newFlags.substring(0, index) + newFlags.substring(index + dartFlag.length() + 1);
      }

      if (StringUtil.isEmptyOrSpaces(newFlags)) {
        envVars.remove(DART_FLAGS_ENV_VAR);
      }
      else {
        envVars.put(DART_FLAGS_ENV_VAR, newFlags);
      }
    }
  }

  public static void removeUnsupportedAsyncFlag() {
    final WebBrowser dartium = getDartiumBrowser();
    final BrowserSpecificSettings browserSpecificSettings = dartium == null ? null : dartium.getSpecificSettings();
    if (!(browserSpecificSettings instanceof ChromeSettings)) return;

    final Map<String, String> envVars = browserSpecificSettings.getEnvironmentVariables();
    setDartFlagState(envVars, ENABLE_ASYNC_OPTION, false);
  }
}
