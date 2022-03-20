package com.jetbrains.cidr.cpp.embedded.platformio;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.PathEnvironmentVariableUtil;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.configurations.TargetAwareRunProfile;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsActions;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.jetbrains.cidr.cpp.cmake.model.CMakeTarget;
import com.jetbrains.cidr.cpp.embedded.platformio.ui.PlatformioActionBase;
import com.jetbrains.cidr.cpp.execution.CMakeAppRunConfiguration;
import com.jetbrains.cidr.cpp.execution.CMakeBuildConfigurationHelper;
import com.jetbrains.cidr.cpp.toolchains.CPPDebugger;
import com.jetbrains.cidr.cpp.toolchains.CPPToolchains;
import com.jetbrains.cidr.execution.CidrCommandLineState;
import com.jetbrains.cidr.execution.CidrExecutableDataHolder;
import com.jetbrains.cidr.toolchains.OSType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.function.Supplier;

@SuppressWarnings("WeakerAccess")
public abstract class PlatformioBaseConfiguration extends CMakeAppRunConfiguration
  implements CidrExecutableDataHolder, TargetAwareRunProfile {

  private final String myBuildTargetName;
  private final Supplier<@NlsActions.ActionText String> mySuggestedName;
  private final String[] cliParameters;


  private volatile CPPToolchains.Toolchain myToolchain;
  private final PlatformioActionBase.FUS_COMMAND command;

  public PlatformioBaseConfiguration(@NotNull Project project,
                                     @NotNull ConfigurationFactory configurationFactory,
                                     @NotNull String myBuildTargetName,
                                     @NotNull Supplier<@NlsActions.ActionText String> name,
                                     @Nullable String[] cliParameters,
                                     @NotNull PlatformioActionBase.FUS_COMMAND command) {
    super(project, configurationFactory, name.get());
    this.myBuildTargetName = myBuildTargetName;
    this.mySuggestedName = name;
    this.cliParameters = cliParameters;
    this.command = command;
  }

  @NotNull
  @Override
  public CMakeBuildConfigurationHelper getHelper() {
    return new CMakeBuildConfigurationHelper(getProject()) {
      @Nullable
      @Override
      public CMakeTarget getDefaultTarget() {
        return findFirstSuitableTarget(myBuildTargetName);
      }
    };
  }

  @Override
  public @Nullable
  CidrCommandLineState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) {
    return new CidrCommandLineState(env, new PlatformioLauncher(env, this, cliParameters, command));
  }


  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    if (myToolchain == null) {
      createToolchain();
    }
    checkSettingsBeforeRun();
  }

  private synchronized void createToolchain() {
    String platformioLocation = findPlatformio();
    if (platformioLocation != null && FileUtil.canExecute(new File(platformioLocation))) {
      myToolchain = new CPPToolchains.Toolchain(OSType.getCurrent());
      myToolchain.setDebugger(CPPDebugger.create(CPPDebugger.Kind.CUSTOM_GDB, platformioLocation));
    }
  }

  public CPPToolchains.Toolchain getToolchain() {
    return myToolchain;
  }

  @Override
  public void checkSettingsBeforeRun() throws RuntimeConfigurationException {
    if (myToolchain == null) {
      throw new RuntimeConfigurationException(ClionEmbeddedPlatformioBundle.message("platformio.not.found.long"));
    }
  }

  @Override
  public String suggestedName() {
    return mySuggestedName.get();
  }

  @Nullable
  public static String findPlatformio() {
    String platformioLocation = PlatformioConfigurable.getPioLocation();
    if (!platformioLocation.isEmpty()) {
      return new File(platformioLocation).canExecute() ? platformioLocation : null;
    }
    if (SystemInfo.isWindows) {
      platformioLocation = PathEnvironmentVariableUtil.findExecutableInWindowsPath("platformio", null);
    }
    else {
      File file = PathEnvironmentVariableUtil.findInPath("platformio");
      platformioLocation = file == null ? null : file.getAbsolutePath();
    }
    return platformioLocation;
  }

  @NotNull
  public abstract String getCmakeBuildTarget();
}
