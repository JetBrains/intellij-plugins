// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.run;

import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationWarning;
import com.intellij.flex.FlexCommonBundle;
import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.model.bc.LinkageType;
import com.intellij.flex.model.bc.OutputType;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.ide.BrowserUtil;
import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.actions.airpackage.AirPackageUtil;
import com.intellij.lang.javascript.flex.build.FlashProjectStructureProblem;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.Factory;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.flex.projectStructure.ui.AirPackagingConfigurableBase;
import com.intellij.lang.javascript.flex.sdk.FlexSdkComboBoxWithBrowseButton;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkType;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ui.configuration.projectRoot.daemon.ProjectStructureProblemType;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public class FlashRunnerParameters extends BCBasedRunnerParameters implements Cloneable {

  public static void checkAirVersionIfCustomDescriptor(final Module module,
                                                       final Sdk sdk,
                                                       final AirPackagingOptions packagingOptions,
                                                       final Consumer<? super FlashProjectStructureProblem> errorConsumer,
                                                       final boolean errorMessageForRunConfigValidation,
                                                       final String bcName) {
    if (packagingOptions.isUseGeneratedDescriptor()) return;
    if (packagingOptions.getCustomDescriptorPath().isEmpty()) return;
    final VirtualFile descriptorFile = LocalFileSystem.getInstance().findFileByPath(packagingOptions.getCustomDescriptorPath());
    if (descriptorFile == null || descriptorFile.isDirectory()) return;
    if (sdk.getSdkType() == FlexmojosSdkType.getInstance()) return;


    final PsiFile file = PsiManager.getInstance(module.getProject()).findFile(descriptorFile);
    final XmlTag rootTag = file instanceof XmlFile ? ((XmlFile)file).getRootTag() : null;
    final String ns = rootTag == null ? null : rootTag.getNamespace();
    final String nsVersion = ns != null && ns.startsWith(FlexCommonUtils.AIR_NAMESPACE_BASE)
                             ? ns.substring(FlexCommonUtils.AIR_NAMESPACE_BASE.length())
                             : null;
    final String airSdkVersion = FlexCommonUtils.getAirVersion(sdk.getHomePath(), sdk.getVersionString());

    if (nsVersion != null && airSdkVersion != null && !nsVersion.equals(airSdkVersion)) {
      final String message;
      if (errorMessageForRunConfigValidation) {
        message = FlexBundle.message("bc.0.module.1.air.version.mismatch.warning", bcName, module.getName(), nsVersion, airSdkVersion,
                                     FileUtil.toSystemDependentName(descriptorFile.getPath()));
      }
      else {
        final String device = packagingOptions instanceof AndroidPackagingOptions
                              ? "Android"
                              : packagingOptions instanceof IosPackagingOptions
                                ? "iOS"
                                : "";
        message = FlexBundle.message("air.version.mismatch.warning", device, descriptorFile.getName(), nsVersion, airSdkVersion);
      }
      errorConsumer.consume(FlashProjectStructureProblem
                              .createPackagingOptionsProblem(ProjectStructureProblemType.Severity.WARNING, packagingOptions, message,
                                                             AirPackagingConfigurableBase.Location.CustomDescriptor));
    }
  }

  public enum AirMobileRunTarget {
    Emulator, AndroidDevice, iOSSimulator, iOSDevice
  }

  public enum Emulator {
    E480("720 x 480", "480", 0, null, 720, 480, 720, 480),
    E720("1280 x 720", "720", 0, null, 1280, 720, 1280, 720),
    E1080("1920 x 1080", "1080", 0, null, 1920, 1080, 1920, 1080),

    iPad("Apple iPad", "iPad", 132, "IOS", 768, 1004, 768, 1024),
    iPadRetina("Apple iPad Retina", "iPadRetina", 132 * 2, "IOS", 768 * 2, 1004 * 2, 768 * 2, 1024 * 2),
    iPhone("Apple iPhone", "iPhone", 163, "IOS", 320, 460, 320, 480),
    iPhoneRetina("Apple iPhone Retina", "iPhoneRetina", 163 * 2, "IOS", 320 * 2, 460 * 2, 320 * 2, 480 * 2),
    //iPod("Apple iPod", "iPod", 163, "IOS", 320, 460, 320, 480),
    //iPodRetina("Apple iPod Retina", "iPodRetina", 163 * 2, "IOS", 320 * 2, 460 * 2, 320 * 2, 480 * 2),

    NexusOne("Google Nexus One", "NexusOne", 252, "AND", 480, 762, 480, 800),
    Droid("Motorola Droid", "Droid", 265, "AND", 480, 816, 480, 854),
    SamsungGalaxyS("Samsung Galaxy S", "SamsungGalaxyS", 233, "AND", 480, 762, 480, 800),
    SamsungGalaxyTab("Samsung Galaxy Tab", "SamsungGalaxyTab", 168, "AND", 600, 986, 600, 1024),

    OtherAndroidDevice("Other Android device...", null, 0, "AND", 0, 0, 0, 0),
    OtherIOSDevice("Other iOS Device...", null, 0, "IOS", 0, 0, 0, 0),

    // kept in enum for compatibility, but not used anywhere any more. Changed to OtherAndroidDevice if loaded.
    Other("Other...", null, 0, null, 0, 0, 0, 0),

    // Following values are kept in enum for compatibility, but not shown in UI any more.
    FWQVGA("FWQVGA", "FWQVGA", 0, null, 240, 432, 240, 432),
    FWVGA("FWVGA", "FWVGA", 0, null, 480, 584, 480, 854),
    HVGA("HVGA", "HVGA", 0, null, 320, 480, 320, 480),
    QVGA("QVGA", "QVGA", 0, null, 240, 320, 240, 320),
    WQVGA("WQVGA", "WQVGA", 0, null, 240, 400, 240, 400),
    WVGA("WVGA", "WVGA", 0, null, 480, 800, 480, 800);

    public static final EnumSet<Emulator> ALL_EMULATORS = EnumSet.range(Emulator.values()[0], OtherIOSDevice);

    public final String name;
    public final @Nullable String adlAlias; // null means that screen size parameters are set by user explicitly
    public final int screenDPI; // 0 means that this option should not be passed to adl
    public final @Nullable String versionPlatform; // null means that this option should not be passed to adl
    public final int screenWidth;
    public final int screenHeight;
    public final int fullScreenWidth;
    public final int fullScreenHeight;

    Emulator(String name,
             @Nullable String adlAlias,
             final int screenDPI,
             final @Nullable String versionPlatform,
             int screenWidth,
             int screenHeight,
             int fullScreenWidth,
             int fullScreenHeight) {
      this.name = name;
      this.adlAlias = adlAlias;
      this.screenDPI = screenDPI;
      this.versionPlatform = versionPlatform;
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
    Android, IOS
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
  private int myScreenDpi = 0;
  private boolean myClearAppDataOnEachLaunch = true;
  private @NotNull String myIOSSimulatorSdkPath = "";
  private @NotNull String myIOSSimulatorDevice = "";
  private boolean myFastPackaging = true;
  private @NotNull AirMobileDebugTransport myDebugTransport = AirMobileDebugTransport.USB;
  private int myUsbDebugPort = AirPackageUtil.DEBUG_PORT_DEFAULT;
  private @NotNull String myEmulatorAdlOptions = "";
  private @NotNull AppDescriptorForEmulator myAppDescriptorForEmulator = AppDescriptorForEmulator.Android;

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

  // parameter can be null if there's some new enum constant in workspace.xml (like OtherAndroidDevice), but user downgraded IntelliJ IDEA
  public void setEmulator(final Emulator emulator) {
    if (Emulator.ALL_EMULATORS.contains(emulator)) {
      myEmulator = emulator;
    }
    else if (emulator == Emulator.Other) {
      myEmulator = Emulator.OtherAndroidDevice;
    }
    // else keep default myEmulator = Emulator.NexusOne
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

  public int getScreenDpi() {
    return myScreenDpi;
  }

  public void setScreenDpi(final int screenDpi) {
    myScreenDpi = screenDpi;
  }

  public boolean isClearAppDataOnEachLaunch() {
    return myClearAppDataOnEachLaunch;
  }

  public void setClearAppDataOnEachLaunch(boolean clear) {
    myClearAppDataOnEachLaunch = clear;
  }

  @NotNull
  public String getIOSSimulatorSdkPath() {
    return myIOSSimulatorSdkPath;
  }

  public void setIOSSimulatorSdkPath(@NotNull final String IOSSimulatorSdkPath) {
    myIOSSimulatorSdkPath = IOSSimulatorSdkPath;
  }

  @NotNull
  public String getIOSSimulatorDevice() {
    return myIOSSimulatorDevice;
  }

  public void setIOSSimulatorDevice(@NotNull String iOSSimulatorDevice) {
    myIOSSimulatorDevice = iOSSimulatorDevice;
  }

  public boolean isFastPackaging() {
    return myFastPackaging;
  }

  public void setFastPackaging(final boolean fastPackaging) {
    myFastPackaging = fastPackaging;
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

  // compatibility (AppDescriptorForEmulator.Generated enum constant removed)
  public void setAppDescriptorForEmulator(final AppDescriptorForEmulator appDescriptorForEmulator) {
    if (appDescriptorForEmulator != null) {
      myAppDescriptorForEmulator = appDescriptorForEmulator;
    }
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

  public void reportWarnings(final Project project) throws RuntimeConfigurationWarning {
    try {
      final Pair<Module, FlexBuildConfiguration> moduleAndBC = super.checkAndGetModuleAndBC(project);
      final Module module = moduleAndBC.first;
      final FlexBuildConfiguration bc = moduleAndBC.second;

      final Ref<String> errorMessageRef = new Ref<>(null);
      final Consumer<FlashProjectStructureProblem> consumer = problem -> errorMessageRef.set(problem.errorMessage);

      if (bc.getTargetPlatform() == TargetPlatform.Desktop) {
        checkAirVersionIfCustomDescriptor(module, bc.getSdk(), bc.getAirDesktopPackagingOptions(), consumer, true, getBCName());
      }
      else if (bc.getTargetPlatform() == TargetPlatform.Mobile) {
        switch (myMobileRunTarget) {
          case Emulator:
            switch (myAppDescriptorForEmulator) {
              case Android -> checkAirVersionIfCustomDescriptor(module, bc.getSdk(), bc.getAndroidPackagingOptions(), consumer, true,
                                                                getBCName());
              case IOS -> checkAirVersionIfCustomDescriptor(module, bc.getSdk(), bc.getIosPackagingOptions(), consumer, true,
                                                            getBCName());
            }
            break;
          case AndroidDevice:
            checkAirVersionIfCustomDescriptor(module, bc.getSdk(), bc.getAndroidPackagingOptions(), consumer, true,
                                              getBCName());
            break;
          case iOSSimulator:
          case iOSDevice:
            checkAirVersionIfCustomDescriptor(module, bc.getSdk(), bc.getIosPackagingOptions(), consumer, true, getBCName());
            break;
        }
      }

      if (!errorMessageRef.isNull()) {
        throw new RuntimeConfigurationWarning(errorMessageRef.get());
      }
    }
    catch (RuntimeConfigurationError e) {/* should be already checked somewhere else */}
  }

  @Override
  public Pair<Module, FlexBuildConfiguration> checkAndGetModuleAndBC(final Project project) throws RuntimeConfigurationError {
    final Pair<Module, FlexBuildConfiguration> moduleAndBC = super.checkAndGetModuleAndBC(project);
    doCheck(moduleAndBC);

    if (myOverrideMainClass) {
      final FlexBuildConfiguration origBC = moduleAndBC.second;
      final ModifiableFlexBuildConfiguration overriddenBC = Factory.getTemporaryCopyForCompilation(origBC);

      overriddenBC.setOutputType(OutputType.Application);

      overriddenBC.setMainClass(myOverriddenMainClass);
      overriddenBC.setOutputFileName(myOverriddenOutputFileName);

      overriddenBC.setRLMs(Collections.emptyList());

      if (origBC.getOutputType() != OutputType.Application) {
        overriddenBC.setUseHtmlWrapper(false);
        overriddenBC.setCssFilesToCompile(Collections.emptyList());

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

      if (BCUtils.canHaveResourceFiles(overriddenBC.getNature()) && !BCUtils.canHaveResourceFiles(origBC.getNature())) {
        overriddenBC.getCompilerOptions().setResourceFilesMode(CompilerOptions.ResourceFilesMode.None);
      }

      overriddenBC.getAndroidPackagingOptions().setPackageFileName(FileUtilRt.getNameWithoutExtension(myOverriddenOutputFileName));
      overriddenBC.getIosPackagingOptions().setPackageFileName(FileUtilRt.getNameWithoutExtension(myOverriddenOutputFileName));

      return Pair.create(moduleAndBC.first, overriddenBC);
    }

    return moduleAndBC;
  }

  private void doCheck(final Pair<Module, FlexBuildConfiguration> moduleAndBC) throws RuntimeConfigurationError {
    final FlexBuildConfiguration bc = moduleAndBC.second;
    final Sdk sdk = bc.getSdk();
    if (sdk == null) {
      throw new RuntimeConfigurationError(
        FlexCommonBundle.message("sdk.not.set.for.bc.0.of.module.1", bc.getName(), moduleAndBC.first.getName()));
    }

    if (myOverrideMainClass) {
      if (myOverriddenMainClass.isEmpty()) {
        throw new RuntimeConfigurationError(FlexBundle.message("main.class.not.set"));
      }

      PsiElement clazz = ActionScriptClassResolver.findClassByQNameStatic(myOverriddenMainClass, moduleAndBC.first.getModuleScope(true));
      if (!(clazz instanceof JSClass)) {
        throw new RuntimeConfigurationError(FlexBundle.message("main.class.not.found", myOverriddenMainClass, bc.getName()));
      }

      // no check until IDEA-83046
      //JSClassChooserDialog.PublicInheritorFilter mainClassFilter = BCUtils.getMainClassFilter(moduleAndBC.first, bc, false);
      //if (!mainClassFilter.value((JSClass)clazz)) {
      //  throw new RuntimeConfigurationError(
      //    FlexBundle.message("main.class.is.not.a.subclass.of", myOverriddenMainClass, mainClassFilter.getSuperClassName()));
      //}

      if (myOverriddenOutputFileName.isEmpty()) {
        throw new RuntimeConfigurationError(FlexBundle.message("output.file.name.not.specified"));
      }
      if (!StringUtil.toLowerCase(myOverriddenOutputFileName).endsWith(".swf")) {
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
          if (myUrl.isEmpty()) throw new RuntimeConfigurationError(FlexBundle.message("flex.run.config.url.not.set"));
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

        if (myLauncherParameters.getLauncherType() == LauncherParameters.LauncherType.Player) {
          if (StringUtil.isEmptyOrSpaces(myLauncherParameters.getPlayerPath())) {
            throw new RuntimeConfigurationError(FlexBundle.message("path.to.flash.player.not.set"));
          }
          if (!new File(myLauncherParameters.getPlayerPath()).exists()) {
            throw new RuntimeConfigurationError(FlexBundle.message("flash.player.not.found", myLauncherParameters.getPresentableText()));
          }
        }
        checkDebuggerSdk();
        break;

      case Desktop:
        checkAdlAndAirRuntime(sdk);
        if (bc.getOutputType() == OutputType.Application) {
          checkCustomDescriptor(bc.getAirDesktopPackagingOptions(), getBCName(), getModuleName());
        }
        break;

      case Mobile:
        switch (myMobileRunTarget) {
          case Emulator:
            checkAdlAndAirRuntime(sdk);

            if (bc.getOutputType() == OutputType.Application) {
              switch (myAppDescriptorForEmulator) {
                case Android -> checkCustomDescriptor(bc.getAndroidPackagingOptions(), getBCName(), getModuleName());
                case IOS -> checkCustomDescriptor(bc.getIosPackagingOptions(), getBCName(), getModuleName());
              }
            }
            break;
          case AndroidDevice:
            if (bc.getOutputType() == OutputType.Application) {
              checkCustomDescriptor(bc.getAndroidPackagingOptions(), getBCName(), getModuleName());
            }
            break;
          case iOSSimulator:
            if (bc.getOutputType() == OutputType.Application) {
              checkCustomDescriptor(bc.getIosPackagingOptions(), getBCName(), getModuleName());
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
            break;
          case iOSDevice:
            if (bc.getOutputType() == OutputType.Application) {
              checkCustomDescriptor(bc.getIosPackagingOptions(), getBCName(), getModuleName());
            }
            break;
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

  public static void checkCustomDescriptor(final AirPackagingOptions packagingOptions, final String bcName, final String moduleName)
    throws RuntimeConfigurationError {
    final boolean android = packagingOptions instanceof AndroidPackagingOptions;
    final boolean ios = packagingOptions instanceof IosPackagingOptions;

    if (android && !((AndroidPackagingOptions)packagingOptions).isEnabled()) {
      throw new RuntimeConfigurationError(FlexBundle.message("android.disabled.in.bc", bcName, moduleName));
    }
    if (ios && !((IosPackagingOptions)packagingOptions).isEnabled()) {
      throw new RuntimeConfigurationError(FlexBundle.message("ios.disabled.in.bc", bcName, moduleName));
    }

    if (!packagingOptions.isUseGeneratedDescriptor()) {
      if (packagingOptions.getCustomDescriptorPath().isEmpty()) {
        final String key =
          android
          ? "bc.0.module.1.android.custom.descriptor.not.set"
          : ios ? "bc.0.module.1.ios.custom.descriptor.not.set" : "bc.0.module.1.custom.descriptor.not.set";
        throw new RuntimeConfigurationError(FlexBundle.message(key, bcName, moduleName));
      }
      else {
        final VirtualFile descriptorFile = LocalFileSystem.getInstance().findFileByPath(packagingOptions.getCustomDescriptorPath());
        if (descriptorFile == null || descriptorFile.isDirectory()) {
          final String key = android
                             ? "bc.0.module.1.android.custom.descriptor.not.found"
                             : ios ? "bc.0.module.1.ios.custom.descriptor.not.found"
                                   : "bc.0.module.1.custom.descriptor.not.found";
          throw new RuntimeConfigurationError(
            FlexBundle.message(key, bcName, moduleName,
                               FileUtil.toSystemDependentName(packagingOptions.getCustomDescriptorPath())));
        }
      }
    }
  }

  public static void checkAdlAndAirRuntime(final @NotNull Sdk sdk) throws RuntimeConfigurationError {
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

  public String suggestUniqueName(final List<RunConfiguration> existingConfigurations) {
    final String suggestedName = suggestName();

    final String[] used = new String[existingConfigurations.size()];
    for (int i = 0; i < existingConfigurations.size(); i++) {
      used[i] = existingConfigurations.get(i).getName();
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

  @Override
  protected FlashRunnerParameters clone() {
    final FlashRunnerParameters clone = (FlashRunnerParameters)super.clone();
    clone.myLauncherParameters = myLauncherParameters.clone();
    return clone;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    FlashRunnerParameters that = (FlashRunnerParameters)o;
    return myOverrideMainClass == that.myOverrideMainClass &&
           myLaunchUrl == that.myLaunchUrl &&
           myRunTrusted == that.myRunTrusted &&
           myScreenWidth == that.myScreenWidth &&
           myScreenHeight == that.myScreenHeight &&
           myFullScreenWidth == that.myFullScreenWidth &&
           myFullScreenHeight == that.myFullScreenHeight &&
           myScreenDpi == that.myScreenDpi &&
           myClearAppDataOnEachLaunch == that.myClearAppDataOnEachLaunch &&
           myFastPackaging == that.myFastPackaging &&
           myUsbDebugPort == that.myUsbDebugPort &&
           myOverriddenMainClass.equals(that.myOverriddenMainClass) &&
           myOverriddenOutputFileName.equals(that.myOverriddenOutputFileName) &&
           myUrl.equals(that.myUrl) &&
           myLauncherParameters.equals(that.myLauncherParameters) &&
           myAdlOptions.equals(that.myAdlOptions) &&
           myAirProgramParameters.equals(that.myAirProgramParameters) &&
           myMobileRunTarget == that.myMobileRunTarget &&
           myEmulator == that.myEmulator &&
           myIOSSimulatorSdkPath.equals(that.myIOSSimulatorSdkPath) &&
           myIOSSimulatorDevice.equals(that.myIOSSimulatorDevice) &&
           myDebugTransport == that.myDebugTransport &&
           myEmulatorAdlOptions.equals(that.myEmulatorAdlOptions) &&
           myAppDescriptorForEmulator == that.myAppDescriptorForEmulator &&
           myDebuggerSdkRaw.equals(that.myDebuggerSdkRaw);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), myOverrideMainClass, myOverriddenMainClass, myOverriddenOutputFileName, myLaunchUrl, myUrl,
                        myLauncherParameters, myRunTrusted, myAdlOptions, myAirProgramParameters, myMobileRunTarget, myEmulator,
                        myScreenWidth, myScreenHeight, myFullScreenWidth, myFullScreenHeight, myScreenDpi, myClearAppDataOnEachLaunch,
                        myIOSSimulatorSdkPath, myIOSSimulatorDevice, myFastPackaging, myDebugTransport, myUsbDebugPort,
                        myEmulatorAdlOptions,
                        myAppDescriptorForEmulator, myDebuggerSdkRaw);
  }
}
