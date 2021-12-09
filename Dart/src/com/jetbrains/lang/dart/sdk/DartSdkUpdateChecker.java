// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.sdk;

import com.intellij.ide.BrowserUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.updateSettings.impl.UpdateChecker;
import com.intellij.openapi.util.Couple;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.io.HttpRequests;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.flutter.FlutterUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DartSdkUpdateChecker {
  public static final String SDK_STABLE_DOWNLOAD_URL = "https://dart.dev/redirects/sdk-download-stable";
  private static final String SDK_DEV_DOWNLOAD_URL = "https://dart.dev/redirects/sdk-download-dev";

  private static final String SDK_STABLE_UPDATE_CHECK_URL =
    "https://storage.googleapis.com/dart-archive/channels/stable/release/latest/VERSION";
  private static final String SDK_DEV_UPDATE_CHECK_URL = "https://storage.googleapis.com/dart-archive/channels/dev/release/latest/VERSION";

  private static final String DART_LAST_SDK_CHECK_KEY = "DART_LAST_SDK_CHECK_KEY";
  private static final long CHECK_INTERVAL = TimeUnit.DAYS.toMillis(1);

  private static final Pattern SEMANTIC_VERSION_PATTERN = Pattern.compile("(\\d+\\.\\d+\\.\\d+)([0-9A-Za-z\\-+.]*)");

  public static void mayBeCheckForSdkUpdate(@NotNull final Project project) {
    if (Registry.is("dart.projects.without.pubspec", false)) return;

    final DartSdkUpdateOption option = DartSdkUpdateOption.getDartSdkUpdateOption();
    if (option == DartSdkUpdateOption.DoNotCheck) return;

    final long lastCheckedMillis = PropertiesComponent.getInstance().getLong(DART_LAST_SDK_CHECK_KEY, 0);
    if (System.currentTimeMillis() - lastCheckedMillis < CHECK_INTERVAL) return;

    final DartSdk sdk = DartSdk.getDartSdk(project);
    if (sdk == null) return;

    if (FlutterUtil.getFlutterRoot(sdk.getHomePath()) != null) return; // Dart SDK inside Flutter SDK is updated using another mechanism

    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      if (!project.isDisposed()) {
        PropertiesComponent.getInstance().setValue(DART_LAST_SDK_CHECK_KEY, String.valueOf(System.currentTimeMillis()));

        final String currentSdkVersion = sdk.getVersion();
        final SdkUpdateInfo sdkUpdateInfo = getSdkUpdateInfo(option);

        if (sdkUpdateInfo != null && compareDartSdkVersions(sdkUpdateInfo.myVersion, currentSdkVersion) > 0) {
          ApplicationManager.getApplication().invokeLater(
            () -> notifySdkUpdateAvailable(project, currentSdkVersion, sdkUpdateInfo.myVersion, sdkUpdateInfo.myDownloadUrl),
            ModalityState.NON_MODAL,
            project.getDisposed()
          );
        }
      }
    });
  }

  @Nullable
  static SdkUpdateInfo getSdkUpdateInfo(@NotNull final DartSdkUpdateOption updateOption) {
    boolean checkForStable = updateOption == DartSdkUpdateOption.Stable || updateOption == DartSdkUpdateOption.StableAndDev;
    boolean checkForDev = updateOption == DartSdkUpdateOption.StableAndDev;

    final SdkUpdateInfo stableSdkInfo = checkForStable ? getSdkUpdateInfo(SDK_STABLE_UPDATE_CHECK_URL, SDK_STABLE_DOWNLOAD_URL) : null;
    final SdkUpdateInfo devSdkInfo = checkForDev ? getSdkUpdateInfo(SDK_DEV_UPDATE_CHECK_URL, SDK_DEV_DOWNLOAD_URL) : null;

    final SdkUpdateInfo sdkUpdateInfo;
    if (stableSdkInfo == null) {
      sdkUpdateInfo = devSdkInfo;
    }
    else if (devSdkInfo == null) {
      sdkUpdateInfo = stableSdkInfo;
    }
    else if (compareDartSdkVersions(devSdkInfo.myVersion, stableSdkInfo.myVersion) > 0) {
      sdkUpdateInfo = devSdkInfo;
    }
    else {
      sdkUpdateInfo = stableSdkInfo;
    }

    return sdkUpdateInfo;
  }

  public static int compareDartSdkVersions(@NotNull final String version1, @NotNull final String version2) {
    // Dart SDK follows Semantic Versioning. There are 3 kind of versions:
    // stable release like "1.11.0"
    // dev preview like "1.11.0-dev.3.0"
    // bleeding edge (nightly build) like "1.11.0-edge.131801"
    // According to spec: "1.11.0" > "1.11.0-edge.131801" > "1.11.0-dev.3.0"

    final Couple<String> version1Parts = getMajorMinorPatchAndRemainder(version1);
    final Couple<String> version2Parts = getMajorMinorPatchAndRemainder(version2);

    if (version1Parts == null || version2Parts == null) {
      // spec violation
      return StringUtil.compareVersionNumbers(version1, version2);
    }

    final String majorMinorPatch1 = version1Parts.first;
    final String remainder1 = version1Parts.second;
    final String majorMinorPatch2 = version2Parts.first;
    final String remainder2 = version2Parts.second;

    final int result = StringUtil.compareVersionNumbers(majorMinorPatch1, majorMinorPatch2);
    if (result != 0 || Objects.equals(remainder1, remainder2)) return result;

    if (remainder1.isEmpty()) return 1;
    if (remainder2.isEmpty()) return -1;
    return StringUtil.compareVersionNumbers(remainder1, remainder2);
  }

  @Nullable
  private static Couple<String> getMajorMinorPatchAndRemainder(@NotNull final String semanticVersion) {
    final Matcher matcher = SEMANTIC_VERSION_PATTERN.matcher(semanticVersion);
    if (matcher.matches()) {
      return Couple.of(matcher.group(1), matcher.group(2));
    }
    return null;
  }

  private static void notifySdkUpdateAvailable(@NotNull final Project project,
                                               @NotNull final String currentSdkVersion,
                                               @NotNull final String availableSdkVersion,
                                               @NotNull final String downloadUrl) {
    final String title = DartBundle.message("dart.sdk.update.title");
    final String message = DartBundle.message("new.dart.sdk.available.for.download..notification", availableSdkVersion, currentSdkVersion);

    UpdateChecker.getNotificationGroup().createNotification(title, message, NotificationType.INFORMATION).setListener((notification, event) -> {
      notification.expire();
      if ("download".equals(event.getDescription())) {
        BrowserUtil.browse(downloadUrl);
      }

      if ("settings".equals(event.getDescription())) {
        DartConfigurable.openDartSettings(project);
      }
    }).notify(project);
  }

  @Nullable
  private static SdkUpdateInfo getSdkUpdateInfo(@NotNull final String updateCheckUrl, @NotNull final String sdkDownloadUrl) {
    try {
      // { "date"     : "2015-05-28",
      //   "version"  : "1.11.0-dev.3.0",
      //   "revision" : "6072062d4185614c32bf96c3ba833dcc18ab4348" }
      final String versionFileContents = HttpRequests.request(updateCheckUrl).readString(null);
      final String version = new JSONObject(versionFileContents).optString("version", null);
      if (version != null) {
        return new SdkUpdateInfo(sdkDownloadUrl, version);
      }
    }
    catch (Exception e) {/* unlucky */}

    return null;
  }

  static class SdkUpdateInfo {
    @NotNull final String myDownloadUrl;
    @NotNull final String myVersion;

    SdkUpdateInfo(@NotNull final String downloadUrl, @NotNull final String version) {
      myDownloadUrl = downloadUrl;
      myVersion = version;
    }
  }
}
