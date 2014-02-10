package com.jetbrains.lang.dart.ide.runner.client;

import com.intellij.CommonBundle;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.ide.browsers.BrowserFamily;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.ide.browsers.WebBrowserBase;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.sdk.DartSdk;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.UUID;

public class DartiumUtil {
  private static final UUID DEFAULT_DARTIUM_ID = UUID.fromString("BFEE1B69-A97D-4338-8BA4-25170ADCBAA6");

  public static final WebBrowser DARTIUM = WebBrowserBase
    .createCustomBrowser(BrowserFamily.CHROME, "Dartium", DEFAULT_DARTIUM_ID, DartIcons.Dartium_16,
                         new NullableComputable<String>() {
                           @Override
                           @Nullable
                           public String compute() {
                             return getDartiumPath();
                           }
                         }, DartBundle.message("dartium.not.configured", CommonBundle.settingsActionPath())
    );

  @Nullable
  public static String getDartiumPath() {
    try {
      return getDartiumPathOrThrowErrorWithQuickFix(null);
    }
    catch (RuntimeConfigurationError error) {
      return null;
    }
  }

  @NotNull
  public static String getDartiumPathOrThrowErrorWithQuickFix(final @Nullable Project project) throws RuntimeConfigurationError {
    final DartSdk sdk = DartSdk.getGlobalDartSdk();
    if (sdk == null) {
      throw createErrorWithQuickFix(project, DartBundle.message("dart.sdk.not.configured"));
    }

    final File sdkDir = new File(sdk.getHomePath());
    if (!sdkDir.isDirectory()) {
      throw createErrorWithQuickFix(project, DartBundle.message("dart.sdk.bad.path", sdkDir.getPath()));
    }

    final File dartDir = sdkDir.getParentFile();
    final String relativePath = SystemInfo.isMac ? "chromium/Chromium.app"
                                                 : SystemInfo.isWindows ? "chromium/chrome.exe"
                                                                        : "chromium/chrome";

    final File dartiumPath = new File(dartDir, relativePath);
    if (!dartiumPath.exists()) {
      throw createErrorWithQuickFix(project, DartBundle.message("dartium.not.found", dartiumPath.getPath()));
    }

    return FileUtil.toSystemIndependentName(dartiumPath.getPath());
  }

  private static RuntimeConfigurationError createErrorWithQuickFix(final @Nullable Project project, final String message) {
    if (project == null) {
      return new RuntimeConfigurationError(message);
    }
    else {
      return new RuntimeConfigurationError(message, new Runnable() {
        @Override
        public void run() {
          ShowSettingsUtil.getInstance().showSettingsDialog(project, DartBundle.message("dart.title"));
        }
      });
    }
  }
}
