package com.intellij.lang.javascript.flex.actions.airmobile;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.actions.ExternalTask;
import com.intellij.lang.javascript.flex.actions.airinstaller.AdtTask;
import com.intellij.lang.javascript.flex.actions.airinstaller.CertificateParameters;
import com.intellij.lang.javascript.flex.actions.airinstaller.PackageAirInstallerAction;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.HyperlinkEvent;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.intellij.lang.javascript.flex.actions.airmobile.MobileAirPackageParameters.IOSPackageType;
import static com.intellij.lang.javascript.flex.actions.airmobile.MobileAirPackageParameters.MobilePlatform;

public class MobileAirUtil {

  public static final int DEBUG_PORT_DEFAULT = 7936;

  private static final String KEYSTORE_FILE_NAME = "idea_temp_keystore.p12";
  private static final String TEMP_CERTIFICATE_NAME = "TempCertificate";
  private static final String TEMP_KEY_TYPE = "1024-RSA";
  public static final String PKCS12_KEYSTORE_TYPE = "PKCS12";
  public static final String TEMP_KEYSTORE_PASSWORD = "a";
  private static final Pattern AIR_VERSION_PATTERN = Pattern.compile("[1-9]\\.[0-9]{1,2}(\\.[0-9]{1,6})*");
  private static final String ADB_RELATIVE_PATH = "/lib/android/bin/adb" + (SystemInfo.isWindows ? ".exe" : "");

  private MobileAirUtil() {
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

  @Nullable
  public static String getAdtVersion(final Project project, final Sdk sdk) {
    final Ref<String> versionRef = new Ref<String>();

    ExternalTask.runWithProgress(new AdtTask(project, sdk) {
      protected void appendAdtOptions(final List<String> command) {
        command.add("-version");
      }

      protected boolean checkMessages() {
        if (myMessages.size() == 1) {
          String output = myMessages.get(0);
          // adt version "1.5.0.7220"
          // 2.6.0.19120

          final String prefix = "adt version \"";
          final String suffix = "\"";

          if (output.startsWith(prefix) && output.endsWith(suffix)) {
            output = output.substring(prefix.length(), output.length() - suffix.length());
          }

          if (AIR_VERSION_PATTERN.matcher(output).matches()) {
            versionRef.set(output);
            return true;
          }
        }

        return false;
      }
    }, FlexBundle.message("checking.air.version"), FlexBundle.message("check.air.version.title"));
    return versionRef.get();
  }

  /**
   * Detects AIR runtime version installed on Android device connected to the computer
   *
   * @return AIR runtime version or empty string if AIR is not installed on the device
   * @throws AdtException if failed to detect AIR runtime version, for example when device is not connected
   */
  public static String getAirRuntimeVersionOnDevice(final Project project, final Sdk sdk) throws AdtException {
    final Ref<String> versionRef = new Ref<String>();

    final boolean ok = ExternalTask.runWithProgress(
      new AdtTask(project, sdk) {
        protected void appendAdtOptions(final List<String> command) {
          command.add("-runtimeVersion");
          command.add("-platform");
          command.add("android");
        }

        protected boolean checkMessages() {
          if (myMessages.size() == 1) {
            final String output = myMessages.get(0);
            // No Devices Detected
            // Failed to find package com.adobe.air
            // 2.6.0.1912

            if (output.startsWith("Failed to find package com.adobe.air")) {
              versionRef.set("");
              return true;
            }
            else if (AIR_VERSION_PATTERN.matcher(output).matches()) {
              versionRef.set(output);
              return true;
            }
          }

          return false;
        }
      }, FlexBundle.message("checking.air.version"), FlexBundle.message("check.air.version.title"));

    if (!ok) {
      throw new AdtException();
    }

    return versionRef.get();
  }

  public static boolean checkAdtVersion(final Module module, final Sdk sdk, final String adtVersion) {
    if (StringUtil.compareVersionNumbers(adtVersion, "2.6") >= 0) {
      return true; // todo checkAdtVersionForPackaging
    }

    final MessageDialogWithHyperlinkListener dialog =
      new MessageDialogWithHyperlinkListener(module.getProject(),
                                             FlexBundle.message("air.mobile.version.problem.title"),
                                             UIUtil.getErrorIcon(),
                                             FlexBundle.message("run.air.mobile.version.problem", sdk.getName(), module.getName(),
                                                                adtVersion));

    dialog.addHyperlinkListener(new HyperlinkAdapter() {
      protected void hyperlinkActivated(final HyperlinkEvent e) {
        dialog.close(DialogWrapper.OK_EXIT_CODE);

        FlexSdkUtils.openModuleConfigurable(module);
      }
    });

    dialog.show();
    return false;
  }

  public static boolean checkAdtVersionForPackaging(final Project project,
                                                    final String adtVersion,
                                                    final MobileAirPackageParameters parameters) {
    String errorMessageStart = null;
    String requiredVersion = null;

    if (parameters.MOBILE_PLATFORM == MobilePlatform.Android &&
        (parameters.ANDROID_PACKAGE_TYPE == MobileAirPackageParameters.AndroidPackageType.NoDebugCaptiveRuntime)) {
      if (StringUtil.compareVersionNumbers(adtVersion, "3.0") < 0) {
        requiredVersion = "3.0";
        errorMessageStart = FlexBundle.message("air.android.captive.packaging.requires.3.0");
      }
    }
    else if (parameters.MOBILE_PLATFORM == MobilePlatform.iOS
        && (parameters.IOS_PACKAGE_TYPE == IOSPackageType.DebugOverNetwork || parameters.IOS_PACKAGE_TYPE == IOSPackageType.Test)
        && parameters.FAST_PACKAGING) {
      if (StringUtil.compareVersionNumbers(adtVersion, "2.7") < 0) {
        requiredVersion = "2.7";
        errorMessageStart = FlexBundle.message("air.mobile.ios.fast.packaging.requires.2.7");
      }
    }
    else if (parameters.MOBILE_PLATFORM == MobilePlatform.iOS) {
      if (StringUtil.compareVersionNumbers(adtVersion, "2.6") < 0) {
        requiredVersion = "2.6";
        errorMessageStart = FlexBundle.message("air.mobile.ios.packaging.requires.2.6");
      }
    }
    else if (StringUtil.compareVersionNumbers(adtVersion, "2.5") < 0) {
      requiredVersion = "2.5";
      errorMessageStart = FlexBundle.message("air.mobile.packaging.requires.2.5");
    }

    if (errorMessageStart != null) {
      Messages.showErrorDialog(project,
                               FlexBundle.message("air.mobile.packaging.version.problem", errorMessageStart,
                                                  parameters.getFlexSdk().getName(), adtVersion, requiredVersion),
                               FlexBundle.message("air.mobile.version.problem.title"));
      return false;
    }

    return true;
  }


  public static boolean checkAirRuntimeOnDevice(final Project project, final Sdk sdk, final String adtVersion) {
    try {
      final String airRuntimeVersion = getAirRuntimeVersionOnDevice(project, sdk);
      final String adtVersionTruncated = truncateVersionString(adtVersion);
      final String airRuntimeVersionTruncated = truncateVersionString(airRuntimeVersion);
      if (airRuntimeVersion.isEmpty() || StringUtil.compareVersionNumbers(adtVersionTruncated, airRuntimeVersionTruncated) > 0) {
        final String message = airRuntimeVersion.isEmpty()
                               ? FlexBundle.message("air.runtime.not.installed", sdk.getName(), adtVersionTruncated)
                               : FlexBundle
                                 .message("update.air.runtime.question", airRuntimeVersionTruncated, sdk.getName(), adtVersionTruncated);
        final int answer =
          Messages.showYesNoDialog(project, message, FlexBundle.message("air.runtime.version.title"), Messages.getQuestionIcon());

        if (answer == -1) {
          return false;
        }
        else if (answer == 0) {
          installAirRuntimeOnDevice(project, sdk, adtVersionTruncated, !airRuntimeVersion.isEmpty());
        }
      }
      return true;
    }
    catch (AdtException ignore) {
      return false;
    }
  }

  private static String truncateVersionString(final String version) {
    final int firstDotIndex = version.indexOf('.');
    final int secondDotIndex = version.indexOf('.', firstDotIndex + 1);
    final int thirdDotIndex = version.indexOf('.', secondDotIndex + 1);
    return thirdDotIndex > 0 ? version.substring(0, thirdDotIndex) : version;
  }

  private static void installAirRuntimeOnDevice(final Project project,
                                                final Sdk sdk,
                                                final String version,
                                                final boolean uninstallExistingBeforeInstalling) {
    if (uninstallExistingBeforeInstalling) {
      ExternalTask.runWithProgress(new AdtTask(project, sdk) {
        protected void appendAdtOptions(final List<String> command) {
          command.add("-uninstallRuntime");
          command.add("-platform");
          command.add("android");
        }
      }, FlexBundle.message("uninstalling.air.runtime"), FlexBundle.message("uninstall.air.runtime.title"));
    }

    ExternalTask.runWithProgress(new AdtTask(project, sdk) {
      protected void appendAdtOptions(final List<String> command) {
        command.add("-installRuntime");
        command.add("-platform");
        command.add("android");
      }
    }, FlexBundle.message("installing.air.runtime", version), FlexBundle.message("install.air.runtime.title"));
  }

  public static boolean packageApk(final Project project, final MobileAirPackageParameters parameters) {
    FileDocumentManager.getInstance().saveAllDocuments();
    return ExternalTask
      .runWithProgress(createMobileAirPackageTask(project, parameters), FlexBundle.message("creating.android.package"),
                       FlexBundle.message("create.android.package.title"));
  }

  static ExternalTask createMobileAirPackageTask(final Project project, final MobileAirPackageParameters parameters) {
    return new AdtTask(project, parameters.getFlexSdk()) {
      protected void appendAdtOptions(List<String> command) {
        command.add("-package");
        command.add("-target");

        switch (parameters.MOBILE_PLATFORM) {
          case Android:
            switch (parameters.ANDROID_PACKAGE_TYPE) {
              case DebugOverNetwork:
                command.add("apk-debug");
                command.add("-connect");
                command.add(parameters.DEBUG_CONNECT_HOST);
                break;
              case DebugOverUSB:
                command.add("apk-debug");
                command.add("-listen");
                command.add(String.valueOf(parameters.DEBUG_LISTEN_PORT));
                break;
              case NoDebug:
                command.add("apk");
                break;
              case NoDebugCaptiveRuntime:
                command.add("apk-captive-runtime");
                break;
            }
            break;
          case iOS:
            switch (parameters.IOS_PACKAGE_TYPE) {
              case DebugOverNetwork:
                command.add(parameters.FAST_PACKAGING ? "ipa-debug-interpreter" : "ipa-debug");
                command.add("-connect");
                command.add(parameters.DEBUG_CONNECT_HOST);
                break;
              case Test:
                command.add(parameters.FAST_PACKAGING ? "ipa-test-interpreter" : "ipa-test");
                break;
              case AdHoc:
                command.add("ipa-ad-hoc");
                break;
              case AppStore:
                command.add("ipa-app-store");
                break;
            }
            break;
        }

        if (parameters.MOBILE_PLATFORM == MobilePlatform.Android && parameters.AIR_DOWNLOAD_URL.length() > 0) {
          command.add("-airDownloadURL");
          command.add(parameters.AIR_DOWNLOAD_URL);
        }

        appendSigningOptions(command, parameters);

        if (parameters.MOBILE_PLATFORM == MobilePlatform.iOS && parameters.PROVISIONING_PROFILE_PATH.length() > 0) {
          command.add("-provisioning-profile");
          command.add(parameters.PROVISIONING_PROFILE_PATH);
        }

        appendPaths(command, parameters);
      }
    };
  }

  public static boolean installApk(final Project project, final Sdk flexSdk, final String apkPath, final String applicationId) {
    return uninstallAndroidApplication(project, flexSdk, applicationId) &&
           ExternalTask.runWithProgress(new AdtTask(project, flexSdk) {
             protected void appendAdtOptions(final List<String> command) {
               command.add("-installApp");
               command.add("-platform");
               command.add("android");
               command.add("-package");
               command.add(apkPath);
             }
           }, FlexBundle.message("installing.0", apkPath.substring(apkPath.lastIndexOf('/') + 1)),
                                        FlexBundle.message("install.android.application.title"));
  }

  private static boolean uninstallAndroidApplication(final Project project, final Sdk flexSdk, final String applicationId) {
    return ExternalTask.runWithProgress(new AdtTask(project, flexSdk) {
      protected void appendAdtOptions(final List<String> command) {
        command.add("-uninstallApp");
        command.add("-platform");
        command.add("android");
        command.add("-appid");
        command.add(applicationId);
      }

      protected boolean checkMessages() {
        if (myMessages.isEmpty() || (myMessages.size() == 1 && myMessages.get(0).equals("Failed to find package " + applicationId))) {
          return true;
        }
        return false;
      }
    }, FlexBundle.message("uninstalling.0", applicationId), FlexBundle.message("uninstall.android.application.title"));
  }

  public static boolean launchAndroidApplication(final Project project, final Sdk flexSdk, final String applicationId) {
    return ExternalTask.runWithProgress(new AdtTask(project, flexSdk) {
      protected void appendAdtOptions(final List<String> command) {
        command.add("-launchApp");
        command.add("-platform");
        command.add("android");
        command.add("-appid");
        command.add(applicationId);
      }
    }, FlexBundle.message("launching.android.application", applicationId), FlexBundle.message("launch.android.application.title"));
  }

  public static void forwardTcpPort(final Project project, final Sdk sdk, final int usbDebugPort) {
    final String adbPath = sdk.getHomePath() + ADB_RELATIVE_PATH;
    final VirtualFile adbExecutable = LocalFileSystem.getInstance().findFileByPath(adbPath);
    final String presentableCommand = "adb forward tcp:" + usbDebugPort + " tcp:" + usbDebugPort;

    if (adbExecutable != null) {
      ExternalTask.runWithProgress(new ExternalTask(project, sdk) {
        protected List<String> createCommandLine() {
          final List<String> command = new ArrayList<String>();
          command.add(adbExecutable.getPath());
          command.add("forward");
          command.add("tcp:" + usbDebugPort);
          command.add("tcp:" + usbDebugPort);
          return command;
        }
      }, presentableCommand, FlexBundle.message("adb.forward.title"));
    }
    else {
      Messages
        .showWarningDialog(project, FlexBundle.message("adb.not.found", FileUtil.toSystemDependentName(adbPath), presentableCommand),
                           FlexBundle.message("adb.forward.title"));
    }
  }

  public static String getLocalHostAddress() {
    try {
      return InetAddress.getLocalHost().getHostAddress();
    }
    catch (UnknownHostException e) {
      return "";
    }
  }

  @Nullable
  public static String getAppIdFromPackage(final String packagePath) {
    ZipFile zipFile = null;
    try {
      zipFile = new ZipFile(packagePath);
      final ZipEntry entry = zipFile.getEntry("assets/META-INF/AIR/application.xml");
      if (entry != null) {
        return FlexUtils.findXMLElement(zipFile.getInputStream(entry), "<application><id>");
      }
    }
    catch (IOException ignore) {/**/}
    finally {
      if (zipFile != null) {
        try {
          zipFile.close();
        }
        catch (IOException ignored) {/**/}
      }
    }

    return null;
  }

  public static String getAppName(final VirtualFile appDescriptor) {
    try {
      final String appName = FlexUtils.findXMLElement(appDescriptor.getInputStream(), "<application><name>");
      if (StringUtil.isNotEmpty(appName)) {
        return appName;
      }
    }
    catch (IOException ignore) {/**/}

    return appDescriptor.getNameWithoutExtension();
  }
}
