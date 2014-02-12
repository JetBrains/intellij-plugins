package com.jetbrains.lang.dart.ide.runner.client;

import com.intellij.ide.browsers.BrowserFamily;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.ide.browsers.WebBrowserManager;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.UUID;

public class DartiumUtil {
  private static final UUID DARTIUM_ID = UUID.fromString("BFEE1B69-A97D-4338-8BA4-25170ADCBAA6");
  private static final String DARTIUM_NAME = "Dartium";

  @Nullable
  public static WebBrowser getDartiumBrowser() {
    final WebBrowser browser = WebBrowserManager.getInstance().findBrowserById(DARTIUM_ID.toString());
    return browser != null ? browser : WebBrowserManager.getInstance().findBrowserById(DARTIUM_NAME);
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

  public static void ensureDartiumBrowserConfigured(final @NotNull String dartiumPath) {
    final WebBrowser browser = getDartiumBrowser();
    if (browser == null) {
      WebBrowserManager.getInstance().addBrowser(DARTIUM_ID, BrowserFamily.CHROME, DARTIUM_NAME, dartiumPath, true,
                                                 BrowserFamily.CHROME.createBrowserSpecificSettings());
    }
    else if (!dartiumPath.equals(browser.getPath())) {
      WebBrowserManager.getInstance().setBrowserPath(browser, dartiumPath, true);
    }
  }
}
