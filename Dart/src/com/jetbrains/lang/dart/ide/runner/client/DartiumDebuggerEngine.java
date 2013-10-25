package com.jetbrains.lang.dart.ide.runner.client;

import com.intellij.CommonBundle;
import com.intellij.chromeConnector.debugger.ChromeDebuggerEngine;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.ide.browsers.BrowsersConfiguration;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.settings.DartSettingsUtil;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class DartiumDebuggerEngine extends ChromeDebuggerEngine {

  private final WebBrowser myDartiumBrowser;

  public DartiumDebuggerEngine() {
    super("dartium");

    myDartiumBrowser = WebBrowser
      .createCustomBrowser(BrowsersConfiguration.BrowserFamily.CHROME, "Dartium", DartIcons.Dartium_16, new NullableComputable<String>() {
        @Nullable
        public String compute() {
          try {
            return getDartiumPath(null);
          }
          catch (RuntimeConfigurationError error) {
            return null;
          }
        }
      }, DartBundle.message("dartium.not.configured", CommonBundle.settingsActionPath()));
  }

  @NotNull
  public WebBrowser getWebBrowser() {
    return myDartiumBrowser;
  }

  public void checkAvailability(final Project project) throws RuntimeConfigurationError {
    getDartiumPath(project);
  }

  @NotNull
  private static String getDartiumPath(final Project project) throws RuntimeConfigurationError {
    final String sdkPath = DartSettingsUtil.getSettings().getSdkPath();
    if (StringUtil.isEmptyOrSpaces(sdkPath)) {
      throwRuntimeConfigurationError(project, DartBundle.message("dart.sdk.not.configured"));
    }

    final File sdkDir = new File(sdkPath);
    if (!sdkDir.isDirectory()) {
      throwRuntimeConfigurationError(project, DartBundle.message("dart.sdk.bad.path", sdkPath));
    }

    final File dartDir = sdkDir.getParentFile();

    final String relativePath = SystemInfo.isMac ? "chromium/Chromium.app"
                                                 : SystemInfo.isWindows ? "chromium/chrome.exe"
                                                                        : "chromium/chrome";

    final File dartiumPath = new File(dartDir, relativePath);
    if (!dartiumPath.exists()) {
      throwRuntimeConfigurationError(project, DartBundle.message("dartium.not.found", dartiumPath.getPath()));
    }
    return FileUtil.toSystemIndependentName(dartiumPath.getPath());
  }

  private static void throwRuntimeConfigurationError(final @Nullable Project project,
                                                     final String message) throws RuntimeConfigurationError {
    if (project == null) {
      throw new RuntimeConfigurationError(message);
    }
    else {
      throw new RuntimeConfigurationError(message, new Runnable() {
        public void run() {
          ShowSettingsUtil.getInstance().showSettingsDialog(project, DartBundle.message("dart.title"));
        }
      });
    }
  }
}
