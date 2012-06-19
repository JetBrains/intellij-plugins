package com.intellij.lang.javascript.flex.actions.airpackage;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.actions.ExternalTask;
import com.intellij.lang.javascript.flex.actions.MessageDialogWithHyperlinkListener;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.run.FlashRunnerParameters;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.application.ApplicationManager;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.intellij.lang.javascript.flex.actions.airpackage.AirPackageProjectParameters.AndroidPackageType;
import static com.intellij.lang.javascript.flex.actions.airpackage.AirPackageProjectParameters.DesktopPackageType;
import static com.intellij.lang.javascript.flex.actions.airpackage.AirPackageProjectParameters.IOSPackageType;
import static com.intellij.lang.javascript.flex.run.FlashRunnerParameters.AirMobileDebugTransport;

public class AirPackageUtil {

  public static final int DEBUG_PORT_DEFAULT = 7936;

  private static final String KEYSTORE_FILE_NAME = "idea_temp_keystore.p12";
  private static final String TEMP_CERTIFICATE_NAME = "TempCertificate";
  private static final String TEMP_KEY_TYPE = "1024-RSA";
  public static final String PKCS12_KEYSTORE_TYPE = "PKCS12";
  public static final String TEMP_KEYSTORE_PASSWORD = "a";
  private static final Pattern AIR_VERSION_PATTERN = Pattern.compile("[1-9]\\.[0-9]{1,2}(\\.[0-9]{1,6})*");
  private static final String ADB_RELATIVE_PATH = "/lib/android/bin/adb" + (SystemInfo.isWindows ? ".exe" : "");

  private AirPackageUtil() {
  }

  public static String getTempKeystorePath() {
    return FileUtil.getTempDirectory() + File.separatorChar + KEYSTORE_FILE_NAME;
  }

  public static boolean ensureCertificateExists(final Project project, Sdk flexSdk) {
    if (!new File(getTempKeystorePath()).exists()) {
      return createCertificate(project, flexSdk);
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

  public static boolean checkAdtVersion(final Module module, final FlexIdeBuildConfiguration bc, final String adtVersion) {
    if (StringUtil.compareVersionNumbers(adtVersion, "2.6") >= 0) {
      return true; // todo checkAdtVersionForPackaging
    }

    final MessageDialogWithHyperlinkListener dialog =
      new MessageDialogWithHyperlinkListener(module.getProject(),
                                             FlexBundle.message("air.mobile.version.problem.title"),
                                             UIUtil.getErrorIcon(),
                                             FlexBundle.message("run.air.mobile.version.problem", bc.getSdk().getName(), bc.getName(),
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

  /*
  public static boolean checkAdtVersionForPackaging(final Project project,
                                                    final String adtVersion,
                                                    final AndroidPackageParameters parameters) {
    String errorMessageStart = null;
    String requiredVersion = null;

    if (parameters.MOBILE_PLATFORM == MobilePlatform.Android &&
        (parameters.ANDROID_PACKAGE_TYPE == AndroidPackageParameters.AndroidPackageType.NoDebugCaptiveRuntime)) {
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
  */


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

  public static boolean packageApk(final Module module,
                                   final FlexIdeBuildConfiguration bc,
                                   final FlashRunnerParameters runnerParameters,
                                   final boolean isDebug) {
    final AndroidPackagingOptions packagingOptions = bc.getAndroidPackagingOptions();
    final boolean tempCertificate = packagingOptions.getSigningOptions().isUseTempCertificate();

    final PasswordStore passwords = tempCertificate
                                    ? null
                                    : AirPackageAction.getPasswords(module.getProject(), Collections.singletonList(packagingOptions));
    if (!tempCertificate && passwords == null) return false; // user canceled

    FileDocumentManager.getInstance().saveAllDocuments();

    final AndroidPackageType packageType = isDebug
                                           ? runnerParameters.getDebugTransport() == AirMobileDebugTransport.Network
                                             ? AndroidPackageType.DebugOverNetwork
                                             : AndroidPackageType.DebugOverUSB
                                           : AndroidPackageType.Release;
    final ExternalTask task = createAndroidPackageTask(module, bc, packageType, false, runnerParameters.getUsbDebugPort(), passwords);
    return ExternalTask.runWithProgress(task, FlexBundle.message("creating.android.package"),
                                        FlexBundle.message("create.android.package.title"));
  }

  public static boolean packageIpaForSimulator(final Module module,
                                               final FlexIdeBuildConfiguration bc,
                                               final FlashRunnerParameters runnerParameters,
                                               final boolean isDebug) {
    final IosPackagingOptions packagingOptions = bc.getIosPackagingOptions();
    final boolean tempCertificate = false; // todo simulator doesn't require real cert

    final PasswordStore passwords = tempCertificate
                                    ? null
                                    : AirPackageAction.getPasswords(module.getProject(), Collections.singletonList(packagingOptions));
    if (!tempCertificate && passwords == null) return false; // user canceled

    FileDocumentManager.getInstance().saveAllDocuments();

    final IOSPackageType packageType = isDebug ? IOSPackageType.DebugOnSimulator : IOSPackageType.TestOnSimulator;
    final ExternalTask task = createIOSPackageTask(module, bc, packageType, true, runnerParameters.getIOSSimulatorSdkPath(), passwords);
    return ExternalTask.runWithProgress(task, FlexBundle.message("creating.ios.package"), FlexBundle.message("create.ios.package.title"));
  }

  public static ExternalTask createAirDesktopTask(final Module module,
                                                  final FlexIdeBuildConfiguration bc,
                                                  final DesktopPackageType packageType,
                                                  final PasswordStore passwords) {
    final AirDesktopPackagingOptions packagingOptions = bc.getAirDesktopPackagingOptions();
    final AirSigningOptions signingOptions = packagingOptions.getSigningOptions();
    final boolean tempCertificate = signingOptions.isUseTempCertificate();

    final String keystorePassword = tempCertificate ? TEMP_KEYSTORE_PASSWORD
                                                    : passwords.getKeystorePassword(signingOptions.getKeystorePath());
    final String keyPassword = tempCertificate || signingOptions.getKeyAlias().isEmpty()
                               ? ""
                               : passwords.getKeyPassword(signingOptions.getKeystorePath(), signingOptions.getKeyAlias());

    return new AdtTask(module.getProject(), bc.getSdk()) {
      protected void appendAdtOptions(List<String> command) {
        switch (packageType) {
          case AirInstaller:
            command.add("-package");
            appendSigningOptions(command, packagingOptions, keystorePassword, keyPassword);
            break;
          case NativeInstaller:
            command.add("-package");
            appendSigningOptions(command, packagingOptions, keystorePassword, keyPassword);
            command.add("-target");
            command.add("native");
            break;
          case CaptiveRuntimeBundle:
            command.add("-package");
            appendSigningOptions(command, packagingOptions, keystorePassword, keyPassword);
            command.add("-target");
            command.add("bundle");
            break;
          case Airi:
            command.add("-prepare");
            break;
        }

        appendPaths(command, module, bc, packagingOptions, null, packageType.getFileExtension());
      }
    };
  }

  public static ExternalTask createAndroidPackageTask(final Module module,
                                                      final FlexIdeBuildConfiguration bc,
                                                      final AndroidPackageType packageType,
                                                      final boolean captiveRuntime,
                                                      final int debugPort,
                                                      final PasswordStore passwords) {
    final AndroidPackagingOptions packagingOptions = bc.getAndroidPackagingOptions();
    final AirSigningOptions signingOptions = packagingOptions.getSigningOptions();
    final boolean tempCertificate = signingOptions.isUseTempCertificate();

    final String keystorePassword = tempCertificate ? TEMP_KEYSTORE_PASSWORD
                                                    : passwords.getKeystorePassword(signingOptions.getKeystorePath());
    final String keyPassword = tempCertificate || signingOptions.getKeyAlias().isEmpty()
                               ? ""
                               : passwords.getKeyPassword(signingOptions.getKeystorePath(), signingOptions.getKeyAlias());

    return new AdtTask(module.getProject(), bc.getSdk()) {
      protected void appendAdtOptions(List<String> command) {
        command.add("-package");
        command.add("-target");

        switch (packageType) {
          case Release:
            command.add(captiveRuntime ? "apk-captive-runtime" : "apk");
            break;
          case DebugOverNetwork:
            command.add("apk-debug");
            command.add("-connect");
            break;
          case DebugOverUSB:
            command.add("apk-debug");
            command.add("-listen");
            command.add(String.valueOf(debugPort));
            break;
        }

        /*
        if (parameters.AIR_DOWNLOAD_URL.length() > 0) {
          command.add("-airDownloadURL");
          command.add(parameters.AIR_DOWNLOAD_URL);
        }
        */

        appendSigningOptions(command, packagingOptions, keystorePassword, keyPassword);
        appendPaths(command, module, bc, packagingOptions, null, ".apk");
      }
    };
  }

  public static ExternalTask createIOSPackageTask(final Module module,
                                                  final FlexIdeBuildConfiguration bc,
                                                  final IOSPackageType packageType,
                                                  final boolean fastPackaging,
                                                  final String iosSDKPath,
                                                  final PasswordStore passwords) {
    final IosPackagingOptions packagingOptions = bc.getIosPackagingOptions();
    final AirSigningOptions signingOptions = packagingOptions.getSigningOptions();

    // temp certificate not applicable for iOS
    final String keystorePassword = passwords.getKeystorePassword(signingOptions.getKeystorePath());
    final String keyPassword = passwords.getKeyPassword(signingOptions.getKeystorePath(), signingOptions.getKeyAlias());

    return new AdtTask(module.getProject(), bc.getSdk()) {
      protected void appendAdtOptions(List<String> command) {
        command.add("-package");
        command.add("-target");

        switch (packageType) {
          case DebugOverNetwork:
            command.add(fastPackaging ? "ipa-debug-interpreter" : "ipa-debug");
            command.add("-connect");
            break;
          case Test:
            command.add(fastPackaging ? "ipa-test-interpreter" : "ipa-test");
            break;
          case TestOnSimulator:
            command.add("ipa-test-interpreter-simulator");
            break;
          case DebugOnSimulator:
            command.add("ipa-debug-interpreter-simulator");
            break;
          case AdHoc:
            command.add("ipa-ad-hoc");
            break;
          case AppStore:
            command.add("ipa-app-store");
            break;
        }

        appendSigningOptions(command, packagingOptions, keystorePassword, keyPassword);

        if (packageType != IOSPackageType.TestOnSimulator && packageType != IOSPackageType.DebugOnSimulator) {
          command.add("-provisioning-profile");
          command.add(FileUtil.toSystemDependentName(signingOptions.getProvisioningProfilePath()));
        }

        appendPaths(command, module, bc, packagingOptions, iosSDKPath, ".ipa");
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

  public static boolean installOnIosSimulator(final Project project,
                                              final Sdk flexSdk,
                                              final String ipaPath,
                                              final String applicationId,
                                              final String iOSSdkPath) {
    return uninstallFromIosSimulator(project, flexSdk, applicationId, iOSSdkPath) &&
           ExternalTask.runWithProgress(new AdtTask(project, flexSdk) {
             protected void appendAdtOptions(final List<String> command) {
               command.add("-installApp");
               command.add("-platform");
               command.add("ios");
               command.add("-platformsdk");
               command.add(iOSSdkPath);
               command.add("-device");
               command.add("ios-simulator");
               command.add("-package");
               command.add(ipaPath);
             }
           }, FlexBundle.message("installing.0", ipaPath.substring(ipaPath.lastIndexOf('/') + 1)),
                                        FlexBundle.message("install.ipa.on.simulator.title"));
  }

  private static boolean uninstallFromIosSimulator(final Project project,
                                                   final Sdk sdk,
                                                   final String applicationId,
                                                   final String iOSSdkPath) {
    return ExternalTask.runWithProgress(new AdtTask(project, sdk) {
      protected void appendAdtOptions(final List<String> command) {
        command.add("-uninstallApp");
        command.add("-platform");
        command.add("ios");
        command.add("-platformsdk");
        command.add(iOSSdkPath);
        command.add("-device");
        command.add("ios-simulator");
        command.add("-appid");
        command.add(applicationId);
      }

      protected boolean checkMessages() {
        return myMessages.isEmpty() ||
               (myMessages.size() == 1 &&
                (myMessages.get(0).startsWith("Application is not installed") ||
                 myMessages.get(0).equals("Failed to find package " + applicationId)));
      }
    }, FlexBundle.message("uninstalling.0", applicationId), FlexBundle.message("uninstall.ios.simulator.application.title"));
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
        return myMessages.isEmpty() || (myMessages.size() == 1 && myMessages.get(0).equals("Failed to find package " + applicationId));
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

  public static boolean launchOnIosSimulator(final Project project,
                                             final Sdk flexSdk,
                                             final String applicationId,
                                             final String iOSSdkPath) {
    return ExternalTask.runWithProgress(new AdtTask(project, flexSdk) {
      protected void appendAdtOptions(final List<String> command) {
        command.add("-launchApp");
        command.add("-platform");
        command.add("ios");
        command.add("-platformsdk");
        command.add(iOSSdkPath);
        command.add("-device");
        command.add("ios-simulator");
        command.add("-appid");
        command.add(applicationId);
      }
    }, FlexBundle.message("launching.ios.application", applicationId), FlexBundle.message("launch.ios.application.title"));
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

  private static boolean createCertificate(final Project project, final Sdk flexSdk) {
    final AdtTask task = new AdtTask(project, flexSdk) {
      protected void appendAdtOptions(List<String> command) {
        command.add("-certificate");
        command.add("-cn");
        command.add(TEMP_CERTIFICATE_NAME);
        command.add(TEMP_KEY_TYPE);
        command.add(getTempKeystorePath());
        command.add(TEMP_KEYSTORE_PASSWORD);
      }
    };

    final boolean ok = ExternalTask.runWithProgress(task, "Creating certificate", "Create Certificate");
    if (ok) {
      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        public void run() {
          LocalFileSystem.getInstance().refreshAndFindFileByPath(getTempKeystorePath());
        }
      });
    }

    return ok;
  }
}
