package com.jetbrains.lang.dart.ide.runner.client;

import com.intellij.ide.browsers.BrowserFamily;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.ide.browsers.WebBrowserManager;
import com.intellij.ide.browsers.chrome.ChromeSettings;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Map;
import java.util.UUID;

public class DartiumUtil {

  private static final String DART_FLAGS_ENV_VAR = "DART_FLAGS";
  public static final String CHECKED_MODE_OPTION = "--checked";

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
    if (dartiumPath.isEmpty()) return DartBundle.message("warning.dartium.path.not.specified");

    final File file = new File(dartiumPath);
    if (SystemInfo.isMac && !file.exists() || !SystemInfo.isMac && !file.isFile()) {
      return DartBundle.message("warning.invalid.dartium.path");
    }

    return null;
  }

  public static void applyDartiumSettings(final @NotNull String dartiumPathFromUI, final @NotNull ChromeSettings dartiumSettingsFromUI) {
    final WebBrowser dartiumInitial = getDartiumBrowser();
    final String dartiumPathInitial = dartiumInitial == null ? null : dartiumInitial.getPath();

    if (!dartiumPathFromUI.isEmpty() && new File(dartiumPathFromUI).exists() && !dartiumPathFromUI.equals(dartiumPathInitial)) {
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
    final String dartFlags = envVars.get(DART_FLAGS_ENV_VAR);
    return dartFlags != null && (dartFlags.trim().equals(CHECKED_MODE_OPTION) ||
                                 dartFlags.startsWith(CHECKED_MODE_OPTION + " ") ||
                                 dartFlags.endsWith(" " + CHECKED_MODE_OPTION) ||
                                 dartFlags.contains(" " + CHECKED_MODE_OPTION + " "));
  }

  public static void setCheckedMode(@NotNull final Map<String, String> envVars, final boolean checkedMode) {
    final boolean oldCheckedMode = isCheckedMode(envVars);
    if (oldCheckedMode == checkedMode) return;

    final String dartFlags = envVars.get(DART_FLAGS_ENV_VAR);
    if (checkedMode) {
      if (dartFlags == null) {
        envVars.put(DART_FLAGS_ENV_VAR, CHECKED_MODE_OPTION);
      }
      else {
        envVars.put(DART_FLAGS_ENV_VAR, dartFlags + " " + CHECKED_MODE_OPTION);
      }
    }
    else {
      String newFlags = dartFlags;
      if (newFlags.trim().equals(CHECKED_MODE_OPTION)) {
        newFlags = "";
      }
      newFlags = StringUtil.trimStart(newFlags, CHECKED_MODE_OPTION + " ");
      newFlags = StringUtil.trimEnd(newFlags, " " + CHECKED_MODE_OPTION);
      final int index = newFlags.indexOf(" " + CHECKED_MODE_OPTION + " ");
      if (index != -1) {
        // keep one space between parts
        newFlags = newFlags.substring(0, index) + newFlags.substring(index + CHECKED_MODE_OPTION.length() + 1);
      }

      if (StringUtil.isEmptyOrSpaces(newFlags)) {
        envVars.remove(DART_FLAGS_ENV_VAR);
      }
      else {
        envVars.put(DART_FLAGS_ENV_VAR, newFlags);
      }
    }
  }
}
