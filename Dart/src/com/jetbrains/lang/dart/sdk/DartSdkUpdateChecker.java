package com.jetbrains.lang.dart.sdk;

import com.intellij.ide.BrowserUtil;
import com.intellij.ide.actions.ShowSettingsUtilImpl;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.updateSettings.impl.UpdateChecker;
import com.intellij.util.io.HttpRequests;
import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.event.HyperlinkEvent;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class DartSdkUpdateChecker {

  public static final String SDK_STABLE_DOWNLOAD_URL = "https://www.dartlang.org/redirects/sdk-download-stable";
  private static final String SDK_DEV_DOWNLOAD_URL = "https://www.dartlang.org/redirects/sdk-download-dev";

  private static final String SDK_STABLE_UPDATE_CHECK_URL =
    "https://storage.googleapis.com/dart-archive/channels/stable/release/latest/VERSION";
  private static final String SDK_DEV_UPDATE_CHECK_URL = "https://storage.googleapis.com/dart-archive/channels/dev/release/latest/VERSION";

  private static final String DART_LAST_SDK_CHECK_KEY = "DART_LAST_SDK_CHECK_KEY";
  private static final long CHECK_INTERVAL = TimeUnit.DAYS.toMillis(1);

  public static void mayBeCheckForSdkUpdate(@NotNull final Project project) {
    final DartSdkUpdateOption option = DartSdkUpdateOption.getDartSdkUpdateOption();
    if (option == DartSdkUpdateOption.DoNotCheck) return;

    final long lastCheckedMillis = PropertiesComponent.getInstance().getOrInitLong(DART_LAST_SDK_CHECK_KEY, 0);
    if (System.currentTimeMillis() - lastCheckedMillis < CHECK_INTERVAL) return;

    final DartSdk sdk = DartSdk.getDartSdk(project);
    if (sdk == null) return;

    final String currentRevisionString = DartSdkUtil.readSdkRevision(sdk.getHomePath());
    if (currentRevisionString == null) return;

    final int currentRevision;
    try {
      currentRevision = Integer.parseInt(currentRevisionString);
    }
    catch (NumberFormatException e) {
      return;
    }

    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      @Override
      public void run() {
        if (!project.isDisposed()) {
          PropertiesComponent.getInstance().setValue(DART_LAST_SDK_CHECK_KEY, String.valueOf(System.currentTimeMillis()));
          doCheckForSdkUpdate(project, sdk.getVersion(), currentRevision, option);
        }
      }
    });
  }

  private static void doCheckForSdkUpdate(@NotNull final Project project,
                                          @NotNull final String currentSdkVersion,
                                          final int currentRevision,
                                          @NotNull final DartSdkUpdateOption updateOption) {
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
    else if (devSdkInfo.myRevision > stableSdkInfo.myRevision) {
      sdkUpdateInfo = devSdkInfo;
    }
    else {
      sdkUpdateInfo = stableSdkInfo;
    }

    if (sdkUpdateInfo != null && sdkUpdateInfo.myRevision > currentRevision) {
      ApplicationManager.getApplication().invokeLater(new Runnable() {
        @Override
        public void run() {
          notifySdkUpdateAvailable(project, currentSdkVersion, sdkUpdateInfo.myPresentableVersion, sdkUpdateInfo.myDownloadUrl);
        }
      }, ModalityState.NON_MODAL, project.getDisposed());
    }
  }

  private static void notifySdkUpdateAvailable(@NotNull final Project project,
                                               @NotNull final String currentSdkVersion,
                                               @NotNull final String availableSdkVersion,
                                               @NotNull final String downloadUrl) {
    final String title = DartBundle.message("dart.sdk.update.title");
    final String message = DartBundle.message("new.dart.sdk.available", availableSdkVersion, currentSdkVersion);
    UpdateChecker.NOTIFICATIONS.createNotification(title, message, NotificationType.INFORMATION, new NotificationListener() {
      @Override
      public void hyperlinkUpdate(@NotNull Notification notification, @NotNull HyperlinkEvent event) {
        notification.expire();
        if ("download".equals(event.getDescription())) {
          BrowserUtil.browse(downloadUrl);
        }

        if ("settings".equals(event.getDescription())) {
          ShowSettingsUtilImpl.showSettingsDialog(project, DartConfigurable.DART_SETTINGS_PAGE_ID, "");
        }
      }
    }).notify(project);
  }

  @Nullable
  private static SdkUpdateInfo getSdkUpdateInfo(@NotNull final String updateCheckUrl, @NotNull final String sdkDownloadUrl) {
    try {
      final String versionFileContents = HttpRequests.request(updateCheckUrl).readString(null);
      final String revisionString = parseRevisionNumberFromJSON(versionFileContents);
      final String presentableVersion = parsePresentableVersionFromJSON(versionFileContents);

      if (revisionString != null && presentableVersion != null) {
        return new SdkUpdateInfo(sdkDownloadUrl, presentableVersion, Integer.parseInt(revisionString));
      }
    }
    catch (Exception e) {/* unlucky */}

    return null;
  }

  /**
   * Parse the revision number from a JSON string.
   * <p>
   * Sample payload:
   * </p>
   * <p/>
   * <pre>
   * {
   *   "revision" : "9826",
   *   "version"  : "0.0.1_v2012070961811",
   *   "date"     : "2012-07-09"
   * }
   * </pre>
   *
   * @param versionJSON the json
   * @return a revision number or <code>null</code> if none can be found
   * @throws IOException
   */
  @Nullable
  private static String parseRevisionNumberFromJSON(final @NotNull String versionJSON) {
    try {
      final JSONObject obj = new JSONObject(versionJSON);
      return obj.optString("revision", null);
    }
    catch (JSONException e) {
      throw null;
    }
  }

  @Nullable
  private static String parsePresentableVersionFromJSON(final @NotNull String versionJSON) {
    try {
      final JSONObject obj = new JSONObject(versionJSON);
      final String version = obj.optString("version", null);
      if (version == null) {
        return null;
      }
      final String revision = obj.optString("revision", null);
      if (revision == null) {
        return version; // Shouldn't happen
      }
      return version + "_r" + revision;
    }
    catch (JSONException e) {
      throw null;
    }
  }

  private static class SdkUpdateInfo {
    @NotNull private final String myDownloadUrl;
    @NotNull private final String myPresentableVersion;
    private final int myRevision;

    public SdkUpdateInfo(@NotNull final String downloadUrl, @NotNull final String presentableVersion, final int revision) {
      myDownloadUrl = downloadUrl;
      myPresentableVersion = presentableVersion;
      myRevision = revision;
    }
  }
}
