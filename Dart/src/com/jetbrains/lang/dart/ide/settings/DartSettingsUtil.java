package com.jetbrains.lang.dart.ide.settings;

import com.intellij.CommonBundle;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.ide.browsers.BrowsersConfiguration;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.javascript.library.JSLibraryMappings;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.webcore.libraries.ScriptingLibraryModel;
import com.jetbrains.lang.dart.DartBundle;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class DartSettingsUtil {

  public static final WebBrowser DARTIUM = WebBrowser
    .createCustomBrowser(BrowsersConfiguration.BrowserFamily.CHROME, "Dartium", DartIcons.Dartium_16, new NullableComputable<String>() {
      @Nullable
      public String compute() {
        return getDartiumPath();
      }
    }, DartBundle.message("dartium.not.configured", CommonBundle.settingsActionPath()));

  public static final String DART_SDK_PATH_PROPERTY_NAME = "dart_sdk_path";

  public static void setSettings(DartSettings settings) {
    final PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
    propertiesComponent.setValue(DART_SDK_PATH_PROPERTY_NAME, settings.getSdkPath());
  }

  @NotNull
  public static DartSettings getSettings() {
    final PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
    final String value = propertiesComponent.getValue(DART_SDK_PATH_PROPERTY_NAME);
    return new DartSettings(value == null ? "" : value);
  }

  public static boolean isDartSDKConfigured(Project project) {
    final JSLibraryMappings mappings = ServiceManager.getService(project, JSLibraryMappings.class);
    return ContainerUtil.exists(mappings.getSingleLibraries(), new Condition<ScriptingLibraryModel>() {
      @Override
      public boolean value(ScriptingLibraryModel model) {
        return DartBundle.message("dart.sdk.name").equals(model.getName());
      }
    });
  }

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
    final String sdkPath = getSettings().getSdkPath();
    if (StringUtil.isEmptyOrSpaces(sdkPath)) {
      throwErrorWithQuickFix(project, DartBundle.message("dart.sdk.not.configured"));
    }

    final File sdkDir = new File(sdkPath);
    if (!sdkDir.isDirectory()) {
      throwErrorWithQuickFix(project, DartBundle.message("dart.sdk.bad.path", sdkPath));
    }

    final File dartDir = sdkDir.getParentFile();

    final String relativePath = SystemInfo.isMac ? "chromium/Chromium.app"
                                                 : SystemInfo.isWindows ? "chromium/chrome.exe"
                                                                        : "chromium/chrome";

    final File dartiumPath = new File(dartDir, relativePath);
    if (!dartiumPath.exists()) {
      throwErrorWithQuickFix(project, DartBundle.message("dartium.not.found", dartiumPath.getPath()));
    }
    return FileUtil.toSystemIndependentName(dartiumPath.getPath());
  }

  private static void throwErrorWithQuickFix(final @Nullable Project project,
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
