package com.intellij.lang.javascript.flex.actions.airinstaller;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.io.FileUtil;

import java.io.File;
import java.util.List;

public class MobileAirTools {

  private static final String KEYSTORE_FILE_NAME = "idea_temp_keystore.p12";
  private static final String TEMP_CERTIFICATE_NAME = "TempCertificate";
  private static final String TEMP_KEY_TYPE = "1024-RSA";
  public static final String TEMP_KEYSTORE_TYPE = "PKCS12";
  public static final String TEMP_KEYSTORE_PASSWORD = "a";

  private MobileAirTools() {
  }

  public static String getTempKeystorePath() {
    return FileUtil.getTempDirectory() + File.separatorChar + KEYSTORE_FILE_NAME;
  }

  public static boolean ensureCertificateExists(final Project project, Sdk flexSdk) {
    if (!new File(getTempKeystorePath()).exists()) {
      final CertificateParameters params =
        new CertificateParameters(new File(getTempKeystorePath()).getPath(), TEMP_CERTIFICATE_NAME, TEMP_KEY_TYPE,
                                  TEMP_KEYSTORE_PASSWORD, "", "", "");
      return PackageAirInstallerAction.createCertificate(project, flexSdk, params);
    }
    return true;
  }

  public static boolean packageApk(final Project project, final AndroidAirPackageParameters parameters) {
    return AdtTask.runWithProgress(createApkTask(project, parameters), "Creating Android package", "Create Android Package");
  }

  private static AdtTask createApkTask(final Project project, final AndroidAirPackageParameters parameters) {
    return new AdtTask(project, parameters.getFlexSdk()) {
      protected void appendAdtOptions(List<String> command) {
        command.add("-package");
        command.add("-target");
        command.add(parameters.IS_DEBUG ? "apk-debug" : "apk");

        if (parameters.IS_DEBUG) {
          if (parameters.IS_DEBUG_CONNECT) {
            command.add("-connect");
            command.add(parameters.DEBUG_CONNECT_HOST);
          }
          else if (parameters.IS_DEBUG_LISTEN) {
            command.add("-listen");
            command.add(String.valueOf(parameters.DEBUG_LISTEN_PORT));
          }
        }

        if (parameters.AIR_DOWNLOAD_URL.length() > 0) {
          command.add("-airDownloadURL");
          command.add(parameters.AIR_DOWNLOAD_URL);
        }

        appendSigningOptions(command, parameters);
        appendPaths(command, parameters);
      }
    };
  }

  public static boolean installApk(final Project project, final Sdk flexSdk, final String apkPath, final String applicationId) {
    return uninstallAndroidApplication(project, flexSdk, applicationId) &&
           AdtTask.runWithProgress(new AdtTask(project, flexSdk) {
               protected void appendAdtOptions(final List<String> command) {
                 command.add("-installApp");
                 command.add("-platform");
                 command.add("android");
                 command.add("-package");
                 command.add(apkPath);
               }
             }, "Installing " + apkPath.substring(apkPath.lastIndexOf('/') + 1), "Install Android Application");
  }

  private static boolean uninstallAndroidApplication(final Project project, final Sdk flexSdk, final String applicationId) {
    return AdtTask.runWithProgress(new AdtTask(project, flexSdk) {
        protected void appendAdtOptions(final List<String> command) {
          command.add("-uninstallApp");
          command.add("-platform");
          command.add("android");
          command.add("-appid");
          command.add(applicationId);
        }

        protected boolean checkMessages(final List<String> messages) {
          if (messages.isEmpty() || (messages.size() == 1 && messages.get(0).equals("Failed to find package " + applicationId))) {
            return true;
          }
          return false;
        }
      }, "Uninstalling " + applicationId, "Uninstall Android Application");
  }

  public static boolean launchAndroidApplication(final Project project, final Sdk flexSdk, final String applicationId) {
    return AdtTask.runWithProgress(new AdtTask(project, flexSdk) {
        protected void appendAdtOptions(final List<String> command) {
          command.add("-launchApp");
          command.add("-platform");
          command.add("android");
          command.add("-appid");
          command.add(applicationId);
        }
      }, "Launching Android application " + applicationId, "Launch Android Application");
  }
}
