// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.actions.airpackage;

import com.intellij.flex.FlexCommonUtils;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.actions.ExternalTask;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.run.BCBasedRunnerParameters;
import com.intellij.lang.javascript.flex.run.FlashRunnerParameters;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.PathUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.intellij.lang.javascript.flex.actions.airpackage.AirPackageProjectParameters.*;
import static com.intellij.lang.javascript.flex.run.FlashRunnerParameters.AirMobileDebugTransport;

public final class AirPackageUtil {

  public static final int DEBUG_PORT_DEFAULT = 7936;

  public static final String TEMP_KEYSTORE_PASSWORD = "keystore_password";

  static final String ADB_RELATIVE_PATH = "/lib/android/bin/adb" + (SystemInfo.isWindows ? ".exe" : "");
  private static final String IDB_RELATIVE_PATH = "/lib/aot/bin/iOSBin/idb" + (SystemInfo.isWindows ? ".exe" : "");

  private static final String PREFERRED_IOS_DEVICE_ID_PROPERTY = "flex.preferred.ios.device.id";
  private static final String PREFERRED_ANDROID_DEVICE_ID_PROPERTY = "flex.preferred.android.device.id";

  private AirPackageUtil() {
  }

  public static String getTempKeystorePath() {
    return FlexCommonUtils.getPathToBundledJar("temp_keystore.p12");
  }

  @Nullable
  public static String getAdtVersion(final Project project, final Sdk sdk) {
    final Ref<String> versionRef = new Ref<>();

    ExternalTask.runWithProgress(new AdtTask(project, sdk) {
      @Override
      protected void appendAdtOptions(final List<String> command) {
        command.add("-version");
      }

      @Override
      protected boolean checkMessages() {
        if (myMessages.size() == 1) {
          FlexCommonUtils.parseAirVersionFromAdtOutput(myMessages.get(0), versionRef);
          if (!versionRef.isNull()) {
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
  public static String getAirRuntimeVersionOnDevice(final Project project,
                                                    final Sdk sdk,
                                                    final FlashRunnerParameters runnerParameters) throws AdtException {
    final Ref<String> versionRef = new Ref<>();

    final boolean ok = ExternalTask.runWithProgress(
      new AdtTask(project, sdk) {
        @Override
        protected void appendAdtOptions(final List<String> command) {
          command.add("-runtimeVersion");
          command.add("-platform");
          command.add("android");

          final DeviceInfo device = runnerParameters.getDeviceInfo();
          if (device != null) {
            command.add("-device");
            command.add(device.DEVICE_ID);
          }
        }

        @Override
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
            else if (FlexCommonUtils.AIR_VERSION_PATTERN.matcher(output).matches()) {
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

  public static boolean checkAdtVersionForPackaging(final Project project,
                                                    final String actualAdtVersion,
                                                    final String requiredAdtVersion,
                                                    final String sdkName, final String errorMessageStart) {
    if (StringUtil.compareVersionNumbers(actualAdtVersion, requiredAdtVersion) < 0) {
      Messages.showErrorDialog(project,
                               FlexBundle.message("air.mobile.packaging.version.problem", errorMessageStart, sdkName,
                                                  actualAdtVersion, requiredAdtVersion),
                               FlexBundle.message("air.mobile.version.problem.title"));
      return false;
    }

    return true;
  }

  public static boolean startAdbServer(final Project project, final Sdk sdk) {
    return ExternalTask.runWithProgress(new ExternalTask(project, sdk) {
      @Override
      protected List<String> createCommandLine() {
        final ArrayList<String> command = new ArrayList<>();
        command.add(sdk.getHomePath() + ADB_RELATIVE_PATH);
        command.add("start-server");
        return command;
      }

      @Override
      protected void scheduleInputStreamReading() {
        // Reading input stream causes hang on Windows because adb starts child process that never exits (IDEA-87648)
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
          try {
            getProcess().waitFor();
          }
          catch (InterruptedException ignore) {/**/}
          finally {
            cancel();
          }
        });
      }
    }, "adb start-server", "ADB Server Start");
  }

  private static void stopAdbServer(final Project project, final Sdk sdk, final String progressTitle, final String frameTitle) {
    ExternalTask.runWithProgress(new ExternalTask(project, sdk) {
      @Override
      protected List<String> createCommandLine() {
        final ArrayList<String> command = new ArrayList<>();
        command.add(sdk.getHomePath() + ADB_RELATIVE_PATH);
        command.add("kill-server");
        return command;
      }

      @Override
      protected boolean checkMessages() {
        return true; // do not care
      }
    }, progressTitle, frameTitle);
  }

  public static boolean checkAirRuntimeOnDevice(final Project project,
                                                final Sdk sdk,
                                                final FlashRunnerParameters runnerParameters,
                                                final String adtVersion) {
    if (StringUtil.compareVersionNumbers(adtVersion, "3.7") >= 0) {
      // since AIR SDK 3.7 all Android packages include captive runtime
      return true;
    }

    try {
      final String airRuntimeVersion = getAirRuntimeVersionOnDevice(project, sdk, runnerParameters);
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
        if (answer == Messages.YES) {
          installAirRuntimeOnDevice(project, sdk, runnerParameters.getDeviceInfo(), adtVersionTruncated, !airRuntimeVersion.isEmpty());
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
                                                final @Nullable DeviceInfo device,
                                                final String version,
                                                final boolean uninstallExistingBeforeInstalling) {
    if (uninstallExistingBeforeInstalling) {
      ExternalTask.runWithProgress(new AdtTask(project, sdk) {
        @Override
        protected void appendAdtOptions(final List<String> command) {
          command.add("-uninstallRuntime");
          command.add("-platform");
          command.add("android");

          if (device != null) {
            command.add("-device");
            command.add(device.DEVICE_ID);
          }
        }
      }, FlexBundle.message("uninstalling.air.runtime"), FlexBundle.message("uninstall.air.runtime.title"));
    }

    ExternalTask.runWithProgress(new AdtTask(project, sdk) {
      @Override
      protected void appendAdtOptions(final List<String> command) {
        command.add("-installRuntime");
        command.add("-platform");
        command.add("android");

        if (device != null) {
          command.add("-device");
          command.add(device.DEVICE_ID);
        }
      }
    }, FlexBundle.message("installing.air.runtime", version), FlexBundle.message("install.air.runtime.title"));
  }

  public static boolean packageApk(final Module module,
                                   final FlexBuildConfiguration bc,
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
                                               final FlexBuildConfiguration bc,
                                               final FlashRunnerParameters runnerParameters,
                                               final boolean isDebug) {
    FileDocumentManager.getInstance().saveAllDocuments();

    final IOSPackageType packageType = isDebug ? IOSPackageType.DebugOnSimulator : IOSPackageType.TestOnSimulator;
    final ExternalTask task = createIOSPackageTask(module, bc, packageType, true, runnerParameters.getIOSSimulatorSdkPath(), 0, null);
    return ExternalTask.runWithProgress(task, FlexBundle.message("creating.ios.package"), FlexBundle.message("create.ios.package.title"));
  }

  public static boolean packageIpaForDevice(final Module module,
                                            final FlexBuildConfiguration bc,
                                            final FlashRunnerParameters runnerParameters,
                                            final String adtVersion,
                                            final boolean isDebug) {
    final IosPackagingOptions packagingOptions = bc.getIosPackagingOptions();
    final PasswordStore passwords = AirPackageAction.getPasswords(module.getProject(), Collections.singletonList(packagingOptions));
    if (passwords == null) return false; // user canceled

    FileDocumentManager.getInstance().saveAllDocuments();

    final IOSPackageType packageType = isDebug
                                       ? runnerParameters.getDebugTransport() == AirMobileDebugTransport.Network
                                         ? IOSPackageType.DebugOverNetwork
                                         : IOSPackageType.DebugOverUSB
                                       : IOSPackageType.Test;

    final Sdk sdk = bc.getSdk();
    assert sdk != null;

    if (packageType == IOSPackageType.DebugOverUSB &&
        !checkAdtVersionForPackaging(module.getProject(), adtVersion, "3.4", sdk.getName(),
                                     FlexBundle.message("air.ios.debug.via.usb.requires.3.4"))) {
      return false;
    }

    if (runnerParameters.isFastPackaging() &&
        !checkAdtVersionForPackaging(module.getProject(), adtVersion, "2.7", sdk.getName(),
                                     FlexBundle.message("air.mobile.ios.fast.packaging.requires.2.7"))) {
      return false;
    }

    if (!checkAdtVersionForPackaging(module.getProject(), adtVersion, "2.6", sdk.getName(),
                                     FlexBundle.message("air.mobile.packaging.requires.2.6"))) {
      return false;
    }

    final ExternalTask task = createIOSPackageTask(module, bc, packageType, runnerParameters.isFastPackaging(),
                                                   bc.getIosPackagingOptions().getSigningOptions().getIOSSdkPath(),
                                                   runnerParameters.getUsbDebugPort(), passwords);
    return ExternalTask.runWithProgress(task, FlexBundle.message("creating.ios.package"), FlexBundle.message("create.ios.package.title"));
  }

  public static ExternalTask createAirDesktopTask(final Module module,
                                                  final FlexBuildConfiguration bc,
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

    final String outputFolder = PathUtil.getParentPath(bc.getActualOutputFilePath());
    final String packageFilePath = outputFolder + "/" + packagingOptions.getPackageFileName() + packageType.getFileExtension();

    return new AdtPackageTask(module.getProject(), bc.getSdk(), packageFilePath) {
      @Override
      protected void appendAdtOptions(List<String> command) {
        switch (packageType) {
          case AirInstaller -> {
            command.add("-package");
            appendSigningOptions(command, packagingOptions, keystorePassword, keyPassword);
          }
          case NativeInstaller -> {
            command.add("-package");
            appendSigningOptions(command, packagingOptions, keystorePassword, keyPassword);
            command.add("-target");
            command.add("native");
          }
          case CaptiveRuntimeBundle -> {
            command.add("-package");
            appendSigningOptions(command, packagingOptions, keystorePassword, keyPassword);
            command.add("-target");
            command.add("bundle");
          }
          case Airi -> command.add("-prepare");
        }

        appendPaths(command, module, bc, packagingOptions, null, packageType.getFileExtension());
      }
    };
  }

  public static ExternalTask createAndroidPackageTask(final Module module,
                                                      final FlexBuildConfiguration bc,
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

    final String outputFolder = PathUtil.getParentPath(bc.getActualOutputFilePath());
    final String packageFilePath = outputFolder + "/" + packagingOptions.getPackageFileName() + ".apk";

    return new AdtPackageTask(module.getProject(), bc.getSdk(), packageFilePath) {
      @Override
      protected void appendAdtOptions(List<String> command) {
        command.add("-package");
        command.add("-target");

        switch (packageType) {
          case Release -> command.add(captiveRuntime ? "apk-captive-runtime" : "apk");
          case DebugOverNetwork -> {
            command.add("apk-debug");
            command.add("-connect");
          }
          case DebugOverUSB -> {
            command.add("apk-debug");
            command.add("-listen");
            command.add(String.valueOf(debugPort));
          }
        }

        if (!AirSigningOptions.ARCH_DEFAULT.equals(packagingOptions.getSigningOptions().getArch())) {
          command.add("-arch");
          command.add(packagingOptions.getSigningOptions().getArch());
        }

        appendSigningOptions(command, packagingOptions, keystorePassword, keyPassword);
        appendPaths(command, module, bc, packagingOptions, null, ".apk");
      }
    };
  }

  public static ExternalTask createIOSPackageTask(final Module module,
                                                  final FlexBuildConfiguration bc,
                                                  final IOSPackageType packageType,
                                                  final boolean fastPackaging,
                                                  final String iosSDKPath,
                                                  final int usbDebugPort,
                                                  final PasswordStore passwords) {
    final IosPackagingOptions packagingOptions = bc.getIosPackagingOptions();
    final AirSigningOptions signingOptions = packagingOptions.getSigningOptions();

    final boolean simulator = packageType == IOSPackageType.TestOnSimulator || packageType == IOSPackageType.DebugOnSimulator;
    // temp certificate not applicable for iOS
    final String keystorePassword = simulator ? TEMP_KEYSTORE_PASSWORD : passwords.getKeystorePassword(signingOptions.getKeystorePath());
    final String keyPassword = simulator ? null : passwords.getKeyPassword(signingOptions.getKeystorePath(), signingOptions.getKeyAlias());

    final String outputFolder = PathUtil.getParentPath(bc.getActualOutputFilePath());
    final String packageFilePath = outputFolder + "/" + packagingOptions.getPackageFileName() + ".ipa";

    return new AdtPackageTask(module.getProject(), bc.getSdk(), packageFilePath) {
      @Override
      protected void appendAdtOptions(List<String> command) {
        command.add("-package");

        final String adtOptions = packagingOptions.getSigningOptions().getADTOptions();
        if (!adtOptions.isEmpty()) {
          final String undocumentedOptions = FlexCommonUtils.removeOptions(adtOptions, "sampler", "hideAneLibSymbols", "useLegacyAOT");
          command.addAll(StringUtil.split(undocumentedOptions, " "));
        }

        command.add("-target");

        switch (packageType) {
          case Test -> command.add(fastPackaging ? "ipa-test-interpreter" : "ipa-test");
          case DebugOverUSB -> {
            command.add(fastPackaging ? "ipa-debug-interpreter" : "ipa-debug");
            command.add("-listen");
            command.add(String.valueOf(usbDebugPort));
          }
          case DebugOverNetwork -> {
            command.add(fastPackaging ? "ipa-debug-interpreter" : "ipa-debug");
            command.add("-connect");
          }
          case TestOnSimulator -> command.add("ipa-test-interpreter-simulator");
          case DebugOnSimulator -> command.add("ipa-debug-interpreter-simulator");
          case AdHoc -> command.add("ipa-ad-hoc");
          case AppStore -> command.add("ipa-app-store");
        }

        if (adtOptions.equals("-sampler") ||
            adtOptions.startsWith("-sampler ") ||
            adtOptions.endsWith(" -sampler") ||
            adtOptions.contains(" -sampler ")) {
          command.add("-sampler");
        }

        final List<String> hideAneLibSymbols = FlexCommonUtils.getOptionValues(adtOptions, "hideAneLibSymbols");
        if (!hideAneLibSymbols.isEmpty()) {
          command.add("-hideAneLibSymbols");
          command.add(hideAneLibSymbols.get(0)); // has one parameter: "yes" or "no"
        }

        final List<String> useLegacyAOT = FlexCommonUtils.getOptionValues(adtOptions, "useLegacyAOT");
        if (!useLegacyAOT.isEmpty()) {
          command.add("-useLegacyAOT");
          command.add(useLegacyAOT.get(0)); // has one parameter: "yes" or "no"
        }

        if (simulator) {
          command.add("-storetype");
          command.add("PKCS12");
          command.add("-keystore");
          command.add(getTempKeystorePath());
          command.add("-storepass");
          command.add(TEMP_KEYSTORE_PASSWORD);
        }
        else {
          appendSigningOptions(command, packagingOptions, keystorePassword, keyPassword);
        }

        if (!simulator) {
          command.add("-provisioning-profile");
          command.add(FileUtil.toSystemDependentName(signingOptions.getProvisioningProfilePath()));
        }

        appendPaths(command, module, bc, packagingOptions, iosSDKPath, ".ipa");
      }
    };
  }

  public static boolean installApk(final Project project,
                                   final Sdk sdk,
                                   final @Nullable DeviceInfo device,
                                   final String apkPath,
                                   final boolean clearDataOnDevice) {
    if (clearDataOnDevice) {
      return ExternalTask.runWithProgress(new AdtTask(project, sdk) {
                                            @Override
                                            protected void appendAdtOptions(final List<String> command) {
                                              command.add("-installApp");
                                              command.add("-platform");
                                              command.add("android");

                                              if (device != null) {
                                                command.add("-device");
                                                command.add(device.DEVICE_ID);
                                              }

                                              command.add("-package");
                                              command.add(apkPath);
                                            }

                                            @Override
                                            protected boolean checkMessages() {
                                              return myMessages.isEmpty() ||
                                                     ContainerUtil.or(myMessages, s -> StringUtil.containsIgnoreCase(s, "success"));
                                            }
                                          }, FlexBundle.message("installing.0", apkPath.substring(apkPath.lastIndexOf('/') + 1)),
                                          FlexBundle.message("install.android.application.title"));
    }
    else {
      return ExternalTask.runWithProgress(new ExternalTask(project, sdk) {
                                            @Override
                                            protected List<String> createCommandLine() {
                                              final ArrayList<String> command = new ArrayList<>();
                                              command.add(sdk.getHomePath() + ADB_RELATIVE_PATH);
                                              if (device != null) {
                                                command.add("-s");
                                                command.add(device.DEVICE_ID);
                                              }

                                              command.add("install");
                                              command.add("-r");
                                              command.add(apkPath);
                                              return command;
                                            }

                                            @Override
                                            protected boolean checkMessages() {
                                              return myMessages.isEmpty() ||
                                                     ContainerUtil.or(myMessages, s -> StringUtil.containsIgnoreCase(s, "success"));
                                            }
                                          }, FlexBundle.message("installing.0", apkPath.substring(apkPath.lastIndexOf('/') + 1)),
                                          FlexBundle.message("install.android.application.title"));
    }
  }

  public static boolean installOnIosSimulator(final Project project,
                                              final Sdk flexSdk,
                                              final String ipaPath,
                                              final String applicationId,
                                              final String iOSSdkPath,
                                              final String simulatorDevice) {
    return uninstallFromIosSimulator(project, flexSdk, applicationId, iOSSdkPath, simulatorDevice) &&
           ExternalTask.runWithProgress(new AdtTask(project, flexSdk) {
                                          @Override
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

                                          @Override
                                          protected void prepareEnvVars(Map<String, String> envVars) {
                                            if (!StringUtil.isEmpty(simulatorDevice)) {
                                              envVars.put("AIR_IOS_SIMULATOR_DEVICE", simulatorDevice);
                                            }
                                          }
                                        }, FlexBundle.message("installing.0", ipaPath.substring(ipaPath.lastIndexOf('/') + 1)),
                                        FlexBundle.message("install.ipa.on.simulator.title"));
  }

  public static boolean installOnIosDevice(final Project project,
                                           final Sdk flexSdk,
                                           final FlashRunnerParameters runnerParameters,
                                           final String ipaPath) {
    return ExternalTask.runWithProgress(new AdtTask(project, flexSdk) {
                                          @Override
                                          protected void appendAdtOptions(final List<String> command) {
                                            command.add("-installApp");
                                            command.add("-platform");
                                            command.add("ios");
                                            //command.add("-platformsdk");
                                            //command.add(iOSSdkPath);

                                            final DeviceInfo device = runnerParameters.getDeviceInfo();
                                            if (device != null) {
                                              command.add("-device");
                                              command.add(String.valueOf(device.IOS_HANDLE));
                                            }

                                            command.add("-package");
                                            command.add(ipaPath);
                                          }
                                        }, FlexBundle.message("installing.0", ipaPath.substring(ipaPath.lastIndexOf('/') + 1)),
                                        FlexBundle.message("install.ios.app.title"));
  }

  public static boolean scanIosDevices(final Project project, final Sdk flexSdk, final BCBasedRunnerParameters runnerParameters) {
    final List<DeviceInfo> devices = DeviceInfo.getIosDevices(project, flexSdk);

    if (devices.isEmpty()) {
      final int choice = Messages
        .showYesNoDialog(project, "No iOS devices connected to the computer", "Error", "Scan Again", "Cancel", Messages.getErrorIcon());
      return choice == Messages.YES && scanIosDevices(project, flexSdk, runnerParameters);
    }
    else if (devices.size() == 1) {
      runnerParameters.setDeviceInfo(devices.get(0));
      return true;
    }
    else {
      final String preferredId = PropertiesComponent.getInstance(project).getValue(PREFERRED_IOS_DEVICE_ID_PROPERTY);
      String preferredPresentableName = null;

      final Collection<String> presentableNames = new ArrayList<>();
      for (DeviceInfo device : devices) {
        final StringBuilder presentableName = new StringBuilder();
        if (device.DEVICE_NAME != null && device.IOS_DEVICE_CLASS != null && !device.DEVICE_NAME.contains(device.IOS_DEVICE_CLASS)) {
          presentableName.append(device.IOS_DEVICE_CLASS).append(' ');
        }
        if (device.DEVICE_NAME != null) {
          presentableName.append('\'').append(device.DEVICE_NAME).append("' ");
        }
        presentableName
          .append('(')
          .append(device.DEVICE_ID, 0, 6)
          .append("...")
          .append(device.DEVICE_ID.substring(device.DEVICE_ID.length() - 6))
          .append(')');

        if (device.DEVICE_ID.equals(preferredId)) {
          preferredPresentableName = presentableName.toString();
        }

        presentableNames.add(presentableName.toString());
      }

      if (preferredPresentableName == null) {
        preferredPresentableName = presentableNames.iterator().next();
      }

      final int choice = Messages.showChooseDialog(project, "Select iOS device", "iOS Device", null,
                                                   ArrayUtilRt.toStringArray(presentableNames), preferredPresentableName);
      if (choice == -1) return false;

      final DeviceInfo selectedDevice = devices.get(choice);
      runnerParameters.setDeviceInfo(selectedDevice);
      PropertiesComponent.getInstance(project).setValue(PREFERRED_IOS_DEVICE_ID_PROPERTY, selectedDevice.DEVICE_ID);
      return true;
    }
  }

  public static boolean scanAndroidDevices(final Project project, final Sdk flexSdk, final BCBasedRunnerParameters runnerParameters) {
    final List<DeviceInfo> devices = DeviceInfo.getAndroidDevices(project, flexSdk);

    if (devices.isEmpty()) {
      final int choice = Messages
        .showYesNoDialog(project, "No Android devices connected to the computer", "Error", "Scan Again", "Cancel", Messages.getErrorIcon());
      return choice == Messages.YES && scanAndroidDevices(project, flexSdk, runnerParameters);
    }
    else if (devices.size() == 1) {
      runnerParameters.setDeviceInfo(devices.get(0));
      return true;
    }
    else {
      final String preferredId = PropertiesComponent.getInstance(project).getValue(PREFERRED_ANDROID_DEVICE_ID_PROPERTY);
      String preferredPresentableName = null;

      final Collection<String> presentableNames = new ArrayList<>();
      for (DeviceInfo device : devices) {
        final StringBuilder presentableName = new StringBuilder();
        if (!"device".equals(device.DEVICE_NAME)) {
          presentableName.append(device.DEVICE_NAME).append(' ');
          presentableName.append('(').append(device.DEVICE_ID).append(')');
        }
        else {
          presentableName.append(device.DEVICE_ID);
        }

        if (device.DEVICE_ID.equals(preferredId)) {
          preferredPresentableName = presentableName.toString();
        }

        presentableNames.add(presentableName.toString());
      }

      if (preferredPresentableName == null) {
        preferredPresentableName = presentableNames.iterator().next();
      }

      final int choice = Messages.showChooseDialog(project, "Select Android device", "Android Device", null,
                                                   ArrayUtilRt.toStringArray(presentableNames), preferredPresentableName);
      if (choice == -1) return false;

      final DeviceInfo selectedDevice = devices.get(choice);
      runnerParameters.setDeviceInfo(selectedDevice);
      PropertiesComponent.getInstance(project).setValue(PREFERRED_ANDROID_DEVICE_ID_PROPERTY, selectedDevice.DEVICE_ID);
      return true;
    }
  }

  private static boolean uninstallFromIosSimulator(final Project project,
                                                   final Sdk sdk,
                                                   final String applicationId,
                                                   final String iOSSdkPath,
                                                   final String simulatorDevice) {
    return ExternalTask.runWithProgress(new AdtTask(project, sdk) {
      @Override
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

      @Override
      protected void prepareEnvVars(Map<String, String> envVars) {
        if (!StringUtil.isEmpty(simulatorDevice)) {
          envVars.put("AIR_IOS_SIMULATOR_DEVICE", simulatorDevice);
        }
      }

      @Override
      protected boolean checkMessages() {
        return myMessages.isEmpty() ||
               (myMessages.size() == 1 &&
                (myMessages.get(0).startsWith("Application is not installed") ||
                 myMessages.get(0).equals("Failed to find package " + applicationId)));
      }
    }, FlexBundle.message("uninstalling.0", applicationId), FlexBundle.message("uninstall.ios.simulator.application.title"));
  }

  public static boolean uninstallAndroidApplication(final Project project,
                                                    final Sdk flexSdk,
                                                    final @Nullable DeviceInfo device,
                                                    final String applicationId) {
    return ExternalTask.runWithProgress(new AdtTask(project, flexSdk) {
      @Override
      protected void appendAdtOptions(final List<String> command) {
        command.add("-uninstallApp");
        command.add("-platform");
        command.add("android");

        if (device != null) {
          command.add("-device");
          command.add(device.DEVICE_ID);
        }

        command.add("-appid");
        command.add(applicationId);
      }

      @Override
      protected boolean checkMessages() {
        return myMessages.isEmpty() || (myMessages.size() == 1 && myMessages.get(0).equals("Failed to find package " + applicationId));
      }
    }, FlexBundle.message("uninstalling.0", applicationId), FlexBundle.message("uninstall.android.application.title"));
  }

  public static boolean launchAndroidApplication(final Project project,
                                                 final Sdk flexSdk,
                                                 final @Nullable DeviceInfo device,
                                                 final String applicationId) {
    return ExternalTask.runWithProgress(new AdtTask(project, flexSdk) {
      @Override
      protected void appendAdtOptions(final List<String> command) {
        command.add("-launchApp");
        command.add("-platform");
        command.add("android");

        if (device != null) {
          command.add("-device");
          command.add(device.DEVICE_ID);
        }

        command.add("-appid");
        command.add(applicationId);
      }
    }, FlexBundle.message("launching.android.application", applicationId), FlexBundle.message("launch.android.application.title"));
  }

  public static boolean launchOnIosSimulator(final Project project,
                                             final Sdk flexSdk,
                                             final String applicationId,
                                             final String iOSSdkPath,
                                             final String simulatorDevice) {
    return ExternalTask.runWithProgress(new AdtTask(project, flexSdk) {
      @Override
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

      @Override
      protected void prepareEnvVars(Map<String, String> envVars) {
        if (!StringUtil.isEmpty(simulatorDevice)) {
          envVars.put("AIR_IOS_SIMULATOR_DEVICE", simulatorDevice);
        }
      }
    }, FlexBundle.message("launching.ios.application", applicationId), FlexBundle.message("launch.ios.application.title"));
  }

  public static boolean androidForwardTcpPort(final Project project,
                                              final Sdk sdk,
                                              final @Nullable DeviceInfo device,
                                              final int usbDebugPort) {
    return ExternalTask.runWithProgress(new ExternalTask(project, sdk) {
      @Override
      protected List<String> createCommandLine() {
        final List<String> command = new ArrayList<>();
        command.add(sdk.getHomePath() + ADB_RELATIVE_PATH);

        if (device != null) {
          command.add("-s");
          command.add(device.DEVICE_ID);
        }

        command.add("forward");
        command.add("tcp:" + usbDebugPort);
        command.add("tcp:" + usbDebugPort);
        return command;
      }
    }, "adb forward tcp:" + usbDebugPort + " tcp:" + usbDebugPort, FlexBundle.message("adb.forward.title"));
  }

  /**
   * Stops adb server and launches idb process that remains alive;
   * caller is responsible to invoke {@link #iosStopForwardTcpPort} when debugging is finished.
   */
  public static boolean iosForwardTcpPort(final Project project, final Sdk sdk, final int usbDebugPort, final int deviceHandle) {
    // running adb server may be forwarding the port that needed for iOS debugging
    stopAdbServer(project, sdk, FlexBundle.message("idb.forward"), FlexBundle.message("idb.forward.title"));

    try {
      // this process remains alive until "idb -stopforward" called
      Runtime.getRuntime().exec(new String[]{
        sdk.getHomePath() + IDB_RELATIVE_PATH,
        "-forward",
        String.valueOf(usbDebugPort),
        String.valueOf(usbDebugPort),
        String.valueOf(deviceHandle)});
    }
    catch (IOException e) {
      Messages.showErrorDialog(project, e.getMessage(), "idb -forward " + usbDebugPort + " " + usbDebugPort + " " + deviceHandle);
      return false;
    }

    return true;
  }

  public static void iosStopForwardTcpPort(final Sdk sdk, final int usbDebugPort) {
    try {
      // this process remains alive until "idb -stopforward" called
      Runtime.getRuntime().exec(new String[]{
        sdk.getHomePath() + IDB_RELATIVE_PATH,
        "-stopforward",
        String.valueOf(usbDebugPort)});
    }
    catch (IOException e) {
      Logger.getInstance(AirPackageUtil.class.getName()).warn(e);
    }
  }

  @Nullable
  public static String getAppIdFromPackage(final String packagePath) {
    try (ZipFile zipFile = new ZipFile(packagePath)) {
      final ZipEntry entry = zipFile.getEntry("assets/META-INF/AIR/application.xml");
      if (entry != null) {
        return FlexUtils.findXMLElement(zipFile.getInputStream(entry), "<application><id>");
      }
    }
    catch (IOException ignore) {/**/}

    return null;
  }
}
