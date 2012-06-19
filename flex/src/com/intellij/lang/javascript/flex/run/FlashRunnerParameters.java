package com.intellij.lang.javascript.flex.run;

import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.ide.BrowserUtil;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.actions.airpackage.AirPackageUtil;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.Factory;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.flex.sdk.FlexSdkComboBoxWithBrowseButton;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkType;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collections;

public class FlashRunnerParameters extends BCBasedRunnerParameters implements Cloneable {

  public enum AirMobileRunTarget {
    Emulator, AndroidDevice, iOSSimulator, iOSDevice
  }

  public enum Emulator {
    E480("720 x 480", "480", 720, 480, 720, 480),
    E720("1280 x 720", "720", 1280, 720, 1280, 720),
    E1080("1920 x 1080", "1080", 1920, 1080, 1920, 1080),
    iPad("Apple iPad", "iPad", 768, 1004, 768, 1024),
    iPhone("Apple iPhone", "iPhone", 320, 460, 320, 480),
    iPhoneRetina("Apple iPhone Retina", "iPhoneRetina", 640, 920, 640, 960),
    //iPod("Apple iPod", "iPod", 320, 460, 320, 480),
    //iPodRetina("Apple iPod Retina", "iPodRetina", 640, 920, 640, 960),
    NexusOne("Google Nexus One", "NexusOne", 480, 762, 480, 800),
    Droid("Motorola Droid", "Droid", 480, 816, 480, 854),
    SamsungGalaxyS("Samsung Galaxy S", "SamsungGalaxyS", 480, 762, 780, 800),
    SamsungGalaxyTab("Samsung Galaxy Tab", "SamsungGalaxyTab", 600, 986, 600, 1024),

    FWQVGA("FWQVGA", "FWQVGA", 240, 432, 240, 432),
    FWVGA("FWVGA", "FWVGA", 480, 584, 480, 854),
    HVGA("HVGA", "HVGA", 320, 480, 320, 480),
    QVGA("QVGA", "QVGA", 240, 320, 240, 320),
    WQVGA("WQVGA", "WQVGA", 240, 400, 240, 400),
    WVGA("WVGA", "WVGA", 480, 800, 480, 800),

    Other("Other...", null, 0, 0, 0, 0);

    public final String name;
    public final String adlAlias;
    public final int screenWidth;
    public final int screenHeight;
    public final int fullScreenWidth;
    public final int fullScreenHeight;

    Emulator(String name, String adlAlias, int screenWidth, int screenHeight, int fullScreenWidth, int fullScreenHeight) {
      this.name = name;
      this.adlAlias = adlAlias;
      this.screenWidth = screenWidth;
      this.screenHeight = screenHeight;
      this.fullScreenWidth = fullScreenWidth;
      this.fullScreenHeight = fullScreenHeight;
    }
  }

  public enum AirMobileDebugTransport {
    Network, USB
  }

  public enum AppDescriptorForEmulator {
    Generated, Android, IOS
  }

  private boolean myOverrideMainClass = false;
  private @NotNull String myOverriddenMainClass = "";
  private @NotNull String myOverriddenOutputFileName = "";

  private boolean myLaunchUrl = false;
  private @NotNull String myUrl = "http://";

  private @NotNull LauncherParameters myLauncherParameters = new LauncherParameters();
  private boolean myRunTrusted = true;

  private @NotNull String myAdlOptions = "";
  private @NotNull String myAirProgramParameters = "";

  private @NotNull AirMobileRunTarget myMobileRunTarget = AirMobileRunTarget.Emulator;
  private @NotNull Emulator myEmulator = Emulator.NexusOne;
  private int myScreenWidth = 0;
  private int myScreenHeight = 0;
  private int myFullScreenWidth = 0;
  private int myFullScreenHeight = 0;
  private String myIOSSimulatorSdkPath = "";
  private @NotNull AirMobileDebugTransport myDebugTransport = AirMobileDebugTransport.USB;
  private int myUsbDebugPort = AirPackageUtil.DEBUG_PORT_DEFAULT;
  private @NotNull String myEmulatorAdlOptions = "";
  private @NotNull AppDescriptorForEmulator myAppDescriptorForEmulator = AppDescriptorForEmulator.Generated;

  private @NotNull String myDebuggerSdkRaw = FlexSdkComboBoxWithBrowseButton.BC_SDK_KEY;

  public boolean isOverrideMainClass() {
    return myOverrideMainClass;
  }

  public void setOverrideMainClass(final boolean overrideMainClass) {
    myOverrideMainClass = overrideMainClass;
  }

  @NotNull
  public String getOverriddenMainClass() {
    return myOverriddenMainClass;
  }

  public void setOverriddenMainClass(@NotNull final String overriddenMainClass) {
    myOverriddenMainClass = overriddenMainClass;
  }

  @NotNull
  public String getOverriddenOutputFileName() {
    return myOverriddenOutputFileName;
  }

  public void setOverriddenOutputFileName(@NotNull final String overriddenOutputFileName) {
    myOverriddenOutputFileName = overriddenOutputFileName;
  }

  public boolean isLaunchUrl() {
    return myLaunchUrl;
  }

  public void setLaunchUrl(final boolean launchUrl) {
    myLaunchUrl = launchUrl;
  }

  @NotNull
  public String getUrl() {
    return myUrl;
  }

  public void setUrl(@NotNull final String url) {
    myUrl = url;
  }

  @NotNull
  public LauncherParameters getLauncherParameters() {
    return myLauncherParameters;
  }

  public void setLauncherParameters(@NotNull final LauncherParameters launcherParameters) {
    myLauncherParameters = launcherParameters;
  }

  public boolean isRunTrusted() {
    return myRunTrusted;
  }

  public void setRunTrusted(final boolean runTrusted) {
    myRunTrusted = runTrusted;
  }

  @NotNull
  public String getAdlOptions() {
    return myAdlOptions;
  }

  public void setAdlOptions(@NotNull final String adlOptions) {
    myAdlOptions = adlOptions;
  }

  @NotNull
  public String getAirProgramParameters() {
    return myAirProgramParameters;
  }

  public void setAirProgramParameters(@NotNull final String airProgramParameters) {
    myAirProgramParameters = airProgramParameters;
  }

  @NotNull
  public AirMobileRunTarget getMobileRunTarget() {
    return myMobileRunTarget;
  }

  public void setMobileRunTarget(@NotNull final AirMobileRunTarget mobileRunTarget) {
    myMobileRunTarget = mobileRunTarget;
  }

  @NotNull
  public Emulator getEmulator() {
    return myEmulator;
  }

  public void setEmulator(@NotNull final Emulator emulator) {
    myEmulator = emulator;
  }

  public int getScreenWidth() {
    return myScreenWidth;
  }

  public void setScreenWidth(final int screenWidth) {
    myScreenWidth = screenWidth;
  }

  public int getScreenHeight() {
    return myScreenHeight;
  }

  public void setScreenHeight(final int screenHeight) {
    myScreenHeight = screenHeight;
  }

  public int getFullScreenWidth() {
    return myFullScreenWidth;
  }

  public void setFullScreenWidth(final int fullScreenWidth) {
    myFullScreenWidth = fullScreenWidth;
  }

  public int getFullScreenHeight() {
    return myFullScreenHeight;
  }

  public void setFullScreenHeight(final int fullScreenHeight) {
    myFullScreenHeight = fullScreenHeight;
  }

  public String getIOSSimulatorSdkPath() {
    return myIOSSimulatorSdkPath;
  }

  public void setIOSSimulatorSdkPath(final String IOSSimulatorSdkPath) {
    myIOSSimulatorSdkPath = IOSSimulatorSdkPath;
  }

  @NotNull
  public AirMobileDebugTransport getDebugTransport() {
    return myDebugTransport;
  }

  public void setDebugTransport(@NotNull final AirMobileDebugTransport debugTransport) {
    myDebugTransport = debugTransport;
  }

  public int getUsbDebugPort() {
    return myUsbDebugPort;
  }

  public void setUsbDebugPort(final int usbDebugPort) {
    myUsbDebugPort = usbDebugPort;
  }

  @NotNull
  public String getEmulatorAdlOptions() {
    return myEmulatorAdlOptions;
  }

  public void setEmulatorAdlOptions(@NotNull final String emulatorAdlOptions) {
    myEmulatorAdlOptions = emulatorAdlOptions;
  }

  @NotNull
  public AppDescriptorForEmulator getAppDescriptorForEmulator() {
    return myAppDescriptorForEmulator;
  }

  public void setAppDescriptorForEmulator(@NotNull final AppDescriptorForEmulator appDescriptorForEmulator) {
    myAppDescriptorForEmulator = appDescriptorForEmulator;
  }

  @NotNull
  public String getDebuggerSdkRaw() {
    return myDebuggerSdkRaw;
  }

  public void setDebuggerSdkRaw(final @NotNull String debuggerSdkRaw) {
    myDebuggerSdkRaw = debuggerSdkRaw;
  }

  public void check(final Project project) throws RuntimeConfigurationError {
    doCheck(super.checkAndGetModuleAndBC(project));
  }

  public Pair<Module, FlexIdeBuildConfiguration> checkAndGetModuleAndBC(final Project project) throws RuntimeConfigurationError {
    final Pair<Module, FlexIdeBuildConfiguration> moduleAndBC = super.checkAndGetModuleAndBC(project);
    doCheck(moduleAndBC);

    if (myOverrideMainClass) {
      final ModifiableFlexIdeBuildConfiguration overriddenBC = Factory.getTemporaryCopyForCompilation(moduleAndBC.second);
      overriddenBC.setMainClass(myOverriddenMainClass);
      overriddenBC.setOutputFileName(myOverriddenOutputFileName);
      overriddenBC.getAndroidPackagingOptions().setPackageFileName(FileUtil.getNameWithoutExtension(myOverriddenOutputFileName));
      overriddenBC.getIosPackagingOptions().setPackageFileName(FileUtil.getNameWithoutExtension(myOverriddenOutputFileName));

      if (overriddenBC.getOutputType() != OutputType.Application) {
        overriddenBC.setOutputType(OutputType.Application);
        overriddenBC.setUseHtmlWrapper(false);
        overriddenBC.setCssFilesToCompile(Collections.<String>emptyList());

        overriddenBC.getDependencies().setFrameworkLinkage(LinkageType.Merged);

        for (ModifiableDependencyEntry entry : overriddenBC.getDependencies().getModifiableEntries()) {
          if (entry.getDependencyType().getLinkageType() == LinkageType.External) {
            entry.getDependencyType().setLinkageType(LinkageType.Merged);
          }
        }

        overriddenBC.getAirDesktopPackagingOptions().setUseGeneratedDescriptor(true);

        final ModifiableAndroidPackagingOptions androidOptions = overriddenBC.getAndroidPackagingOptions();
        androidOptions.setEnabled(true);
        androidOptions.setUseGeneratedDescriptor(true);
        androidOptions.getSigningOptions().setUseTempCertificate(true);

        // impossible without extra user input: certificate and provisioning profile
        overriddenBC.getIosPackagingOptions().setEnabled(false);
      }

      if (BCUtils.canHaveResourceFiles(overriddenBC.getNature()) && !BCUtils.canHaveResourceFiles(moduleAndBC.second.getNature())) {
        overriddenBC.getCompilerOptions().setResourceFilesMode(CompilerOptions.ResourceFilesMode.None);
      }

      return Pair.create(moduleAndBC.first, ((FlexIdeBuildConfiguration)overriddenBC));
    }

    return moduleAndBC;
  }

  private void doCheck(final Pair<Module, FlexIdeBuildConfiguration> moduleAndBC) throws RuntimeConfigurationError {
    final FlexIdeBuildConfiguration bc = moduleAndBC.second;
    final Sdk sdk = bc.getSdk();
    if (sdk == null) {
      throw new RuntimeConfigurationError(
        FlexBundle.message("sdk.not.set.for.bc.0.of.module.1", bc.getName(), moduleAndBC.first.getName()));
    }

    if (myOverrideMainClass) {
      if (myOverriddenMainClass.isEmpty()) {
        throw new RuntimeConfigurationError(FlexBundle.message("main.class.not.set"));
      }

      PsiElement clazz =
        JSResolveUtil.unwrapProxy(JSResolveUtil.findClassByQName(myOverriddenMainClass, moduleAndBC.first.getModuleScope(true)));
      if (!(clazz instanceof JSClass)) {
        throw new RuntimeConfigurationError(FlexBundle.message("main.class.not.found", myOverriddenMainClass, bc.getName()));
      }

      // no check until IDEA-83046
      //JSClassChooserDialog.PublicInheritor mainClassFilter = BCUtils.getMainClassFilter(moduleAndBC.first, bc, false);
      //if (!mainClassFilter.value((JSClass)clazz)) {
      //  throw new RuntimeConfigurationError(
      //    FlexBundle.message("main.class.is.not.a.subclass.of", myOverriddenMainClass, mainClassFilter.getSuperClassName()));
      //}

      if (myOverriddenOutputFileName.isEmpty()) {
        throw new RuntimeConfigurationError(FlexBundle.message("output.file.name.not.specified"));
      }
      if (!myOverriddenOutputFileName.toLowerCase().endsWith(".swf")) {
        throw new RuntimeConfigurationError(FlexBundle.message("output.file.must.have.swf.extension"));
      }
    }
    else {
      if (bc.getOutputType() != OutputType.Application) {
        throw new RuntimeConfigurationError(FlexBundle.message("bc.does.not.produce.app", getBCName(), getModuleName()));
      }
    }

    switch (bc.getTargetPlatform()) {
      case Web:
        if (myLaunchUrl) {
          try {
            if (BrowserUtil.getURL(myUrl) == null) throw new RuntimeConfigurationError(FlexBundle.message("flex.run.config.incorrect.url"));
          }
          catch (MalformedURLException e) {
            throw new RuntimeConfigurationError(FlexBundle.message("flex.run.config.incorrect.url"));
          }

          //if (myLauncherParameters.getLauncherType() == LauncherParameters.LauncherType.Player) {
          //  throw new RuntimeConfigurationError(FlexBundle.message("flex.run.config.url.can.not.be.run.with.flash.player"));
          //}
        }

        //if (myLauncherParameters.getLauncherType() == LauncherParameters.LauncherType.Player
        //    && bc.getTargetPlatform() == TargetPlatform.Web && bc.isUseHtmlWrapper()) {
        //  throw new RuntimeConfigurationError(FlexBundle.message("html.wrapper.can.not.be.run.with.flash.player"));
        //}

        checkDebuggerSdk();
        break;

      case Desktop:
        checkAdlAndAirRuntime(sdk);
        checkCustomDescriptor(bc.getAirDesktopPackagingOptions());
        break;

      case Mobile:
        if (myMobileRunTarget == AirMobileRunTarget.Emulator) {
          checkAdlAndAirRuntime(sdk);

          switch (myAppDescriptorForEmulator) {
            case Generated:
              break;
            case Android:
              checkCustomDescriptor(bc.getAndroidPackagingOptions());
              break;
            case IOS:
              checkCustomDescriptor(bc.getIosPackagingOptions());
              break;
          }
        }
        else if (myMobileRunTarget == AirMobileRunTarget.AndroidDevice) {
          if (bc.getOutputType() == OutputType.Application) {
            checkCustomDescriptor(bc.getAndroidPackagingOptions());
          }
        }
        else if (myMobileRunTarget == AirMobileRunTarget.iOSSimulator) {
          if (bc.getOutputType() == OutputType.Application) {
            checkCustomDescriptor(bc.getIosPackagingOptions());
            // todo check cert
          }

          if (!SystemInfo.isMac) {
            throw new RuntimeConfigurationError(FlexBundle.message("ios.simulator.on.mac.only.warning"));
          }

          if (myIOSSimulatorSdkPath.isEmpty()) {
            throw new RuntimeConfigurationError(FlexBundle.message("ios.simulator.sdk.not.set"));
          }
          else if (!new File(FileUtil.toSystemDependentName(myIOSSimulatorSdkPath)).isDirectory()) {
            throw new RuntimeConfigurationError(
              FlexBundle.message("ios.simulator.sdk.not.found", FileUtil.toSystemDependentName(myIOSSimulatorSdkPath)));
          }
        }

        break;
    }
  }

  protected void checkDebuggerSdk() throws RuntimeConfigurationError {
    if (!myDebuggerSdkRaw.equals(FlexSdkComboBoxWithBrowseButton.BC_SDK_KEY)) {
      final Sdk sdk = FlexSdkUtils.findFlexOrFlexmojosSdk(myDebuggerSdkRaw);
      if (sdk == null) {
        throw new RuntimeConfigurationError(FlexBundle.message("debugger.sdk.not.found", myDebuggerSdkRaw));
      }
    }
  }

  private void checkCustomDescriptor(final AirPackagingOptions packagingOptions) throws RuntimeConfigurationError {
    final boolean android = packagingOptions instanceof AndroidPackagingOptions;
    final boolean ios = packagingOptions instanceof IosPackagingOptions;

    if (android && !((AndroidPackagingOptions)packagingOptions).isEnabled()) {
      throw new RuntimeConfigurationError(FlexBundle.message("android.disabled.in.bc", getBCName(), getModuleName()));
    }
    if (ios && !((IosPackagingOptions)packagingOptions).isEnabled()) {
      throw new RuntimeConfigurationError(FlexBundle.message("ios.disabled.in.bc", getBCName(), getModuleName()));
    }

    if (!packagingOptions.isUseGeneratedDescriptor()) {
      if (packagingOptions.getCustomDescriptorPath().isEmpty()) {
        final String key =
          android
          ? "bc.0.module.1.android.custom.descriptor.not.set"
          : ios ? "bc.0.module.1.ios.custom.descriptor.not.set" : "bc.0.module.1.custom.descriptor.not.set";
        throw new RuntimeConfigurationError(FlexBundle.message(key, getBCName(), getModuleName()));
      }
      else {
        final VirtualFile descriptorFile = LocalFileSystem.getInstance().findFileByPath(packagingOptions.getCustomDescriptorPath());
        if (descriptorFile == null || descriptorFile.isDirectory()) {
          final String key = android
                             ? "bc.0.module.1.android.custom.descriptor.not.found"
                             : ios ? "bc.0.module.1.ios.custom.descriptor.not.found"
                                   : "bc.0.module.1.custom.descriptor.not.found";
          throw new RuntimeConfigurationError(
            FlexBundle.message(key, getBCName(), getModuleName(),
                               FileUtil.toSystemDependentName(packagingOptions.getCustomDescriptorPath())));
        }
      }
    }
  }

  private static void checkAdlAndAirRuntime(final @NotNull Sdk sdk) throws RuntimeConfigurationError {
    final String adlPath = FlexSdkUtils.getAdlPath(sdk);
    if (StringUtil.isEmpty(adlPath)) {
      throw new RuntimeConfigurationError(FlexBundle.message("adl.not.set.check.sdk.settings", sdk.getName()));
    }
    final VirtualFile adlFile = LocalFileSystem.getInstance().findFileByPath(adlPath);
    if (adlFile == null || adlFile.isDirectory()) {
      throw new RuntimeConfigurationError(sdk.getSdkType() instanceof FlexmojosSdkType
                                          ? FlexBundle.message("adl.not.found.check.sdk.settings", adlPath, sdk.getName())
                                          : FlexBundle.message("adl.not.found.check.sdk.installation", adlPath, sdk.getName()));
    }

    if (sdk.getSdkType() instanceof FlexmojosSdkType) {
      final String airRuntimePath = FlexSdkUtils.getAirRuntimePathForFlexmojosSdk(sdk);
      if (StringUtil.isEmpty(airRuntimePath)) {
        throw new RuntimeConfigurationError(FlexBundle.message("air.runtime.not.set.check.sdk.settings", sdk.getName()));
      }
      final VirtualFile airRuntimeDir = LocalFileSystem.getInstance().findFileByPath(airRuntimePath);
      if (airRuntimeDir == null) {
        throw new RuntimeConfigurationError(FlexBundle.message("air.runtime.not.found.check.sdk.settings", airRuntimePath, sdk.getName()));
      }
    }
  }

  public String suggestName() {
    return myOverrideMainClass ? StringUtil.getShortName(myOverriddenMainClass)
                               : getBCName().equals(getModuleName())
                                 ? getBCName()
                                 : (getBCName() + " (" + getModuleName() + ")");
  }

  public String suggestUniqueName(final RunConfiguration[] existingConfigurations) {
    final String suggestedName = suggestName();

    final String[] used = new String[existingConfigurations.length];
    for (int i = 0; i < existingConfigurations.length; i++) {
      used[i] = existingConfigurations[i].getName();
    }

    if (ArrayUtil.contains(suggestedName, used)) {
      int i = 1;
      String name;
      while (ArrayUtil.contains((name = suggestedName + " (" + i + ")"), used)) {
        i++;
      }
      return name;
    }

    return suggestedName;
  }

  protected FlashRunnerParameters clone() {
    final FlashRunnerParameters clone = (FlashRunnerParameters)super.clone();
    clone.myLauncherParameters = myLauncherParameters.clone();
    return clone;
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    final FlashRunnerParameters that = (FlashRunnerParameters)o;

    if (myFullScreenHeight != that.myFullScreenHeight) return false;
    if (myFullScreenWidth != that.myFullScreenWidth) return false;
    if (myLaunchUrl != that.myLaunchUrl) return false;
    if (myOverrideMainClass != that.myOverrideMainClass) return false;
    if (myRunTrusted != that.myRunTrusted) return false;
    if (myScreenHeight != that.myScreenHeight) return false;
    if (myScreenWidth != that.myScreenWidth) return false;
    if (myUsbDebugPort != that.myUsbDebugPort) return false;
    if (!myAdlOptions.equals(that.myAdlOptions)) return false;
    if (!myAirProgramParameters.equals(that.myAirProgramParameters)) return false;
    if (myDebugTransport != that.myDebugTransport) return false;
    if (myEmulator != that.myEmulator) return false;
    if (!myEmulatorAdlOptions.equals(that.myEmulatorAdlOptions)) return false;
    if (!myLauncherParameters.equals(that.myLauncherParameters)) return false;
    if (myMobileRunTarget != that.myMobileRunTarget) return false;
    if (!myOverriddenMainClass.equals(that.myOverriddenMainClass)) return false;
    if (!myOverriddenOutputFileName.equals(that.myOverriddenOutputFileName)) return false;
    if (!myUrl.equals(that.myUrl)) return false;

    return true;
  }
}
