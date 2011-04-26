package com.intellij.lang.javascript.flex.actions.airmobile;

import com.intellij.facet.FacetManager;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexFacet;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.actions.ExternalTask;
import com.intellij.lang.javascript.flex.actions.airinstaller.AdtTask;
import com.intellij.lang.javascript.flex.actions.airinstaller.CertificateParameters;
import com.intellij.lang.javascript.flex.actions.airinstaller.PackageAirInstallerAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectBundle;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.intellij.lang.javascript.flex.actions.airmobile.MobileAirPackageParameters.MobilePlatform;

public class MobileAirUtil {

  public static final int DEBUG_PORT_DEFAULT = 7936;

  private static final String KEYSTORE_FILE_NAME = "idea_temp_keystore.p12";
  private static final String TEMP_CERTIFICATE_NAME = "TempCertificate";
  private static final String TEMP_KEY_TYPE = "1024-RSA";
  public static final String PKCS12_KEYSTORE_TYPE = "PKCS12";
  public static final String TEMP_KEYSTORE_PASSWORD = "a";
  private static final Pattern ADT_VERSION_PATTERN = Pattern.compile("[1-9]\\.[0-9]{1,2}(\\.[0-9]{1,6})*");
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

        protected boolean checkMessages(final List<String> messages) {
          if (messages.size() == 1) {
            String output = messages.get(0);
            // adt version "1.5.0.7220"
            // 2.6.0.19120

            final String prefix = "adt version \"";
            final String suffix = "\"";

            if (output.startsWith(prefix) && output.endsWith(suffix)) {
              output = output.substring(prefix.length(), output.length() - suffix.length());
            }

            if (ADT_VERSION_PATTERN.matcher(output).matches()) {
              versionRef.set(output);
              return true;
            }
          }

          return false;
        }
      }, "Checking AIR version", "Check AIR Version");
    return versionRef.get();
  }

  public static boolean checkAdtVersion(final Module module, final Sdk sdk) {
    final String adtVersion = getAdtVersion(module.getProject(), sdk);
    if (adtVersion != null) {
      if (StringUtil.compareVersionNumbers(adtVersion, "2.6") >= 0) {
        return true;
      }

      final MessageDialogWithHyperlinkListener dialog =
        new MessageDialogWithHyperlinkListener(module.getProject(),
                                               FlexBundle.message("air.mobile.version.problem.title"),
                                               UIUtil.getErrorIcon(),
                                               FlexBundle.message("air.mobile.version.problem", sdk.getName(), module.getName(),
                                                                  adtVersion));

      dialog.addHyperlinkListener(new HyperlinkAdapter() {
        protected void hyperlinkActivated(final HyperlinkEvent e) {
          dialog.close(DialogWrapper.OK_EXIT_CODE);

          final ProjectStructureConfigurable projectStructureConfigurable = ProjectStructureConfigurable.getInstance(module.getProject());
          ShowSettingsUtil.getInstance().editConfigurable(module.getProject(), projectStructureConfigurable, new Runnable() {
            public void run() {
              if (module.getModuleType() instanceof FlexModuleType) {
                projectStructureConfigurable.select(module.getName(), ProjectBundle.message("modules.classpath.title"), true);
              }
              else {
                final FlexFacet facet = FacetManager.getInstance(module).getFacetByType(FlexFacet.ID);
                if (facet != null) {
                  projectStructureConfigurable.select(facet, true);
                }
              }
            }
          });
        }
      });

      dialog.show();
    }

    return false;
  }

  public static boolean packageApk(final Project project, final MobileAirPackageParameters parameters) {
    return ExternalTask
      .runWithProgress(createMobileAirPackageTask(project, parameters), "Creating Android package", "Create Android Package");
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
            }
            break;
          case iOS:
            switch (parameters.IOS_PACKAGE_TYPE) {
              case DebugOverNetwork:
                command.add("ipa-debug");
                command.add("-connect");
                command.add(parameters.DEBUG_CONNECT_HOST);
                break;
              case Test:
                command.add("ipa-test");
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
             }, "Installing " + apkPath.substring(apkPath.lastIndexOf('/') + 1), "Install Android Application");
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

        protected boolean checkMessages(final List<String> messages) {
          if (messages.isEmpty() || (messages.size() == 1 && messages.get(0).equals("Failed to find package " + applicationId))) {
            return true;
          }
          return false;
        }
      }, "Uninstalling " + applicationId, "Uninstall Android Application");
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
}
