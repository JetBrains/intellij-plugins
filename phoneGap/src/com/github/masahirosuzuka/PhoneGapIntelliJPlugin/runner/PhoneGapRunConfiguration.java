// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.PhoneGapBundle;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapAndroidTargets;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapCommandLine;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapIosTargets;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner.ui.PhoneGapRunConfigurationEditor;
import com.intellij.diagnostic.logging.DefaultLogFilterModel;
import com.intellij.diagnostic.logging.LogConsole;
import com.intellij.diagnostic.logging.LogFilterModel;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.EnvironmentUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jdom.Element;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapCommandLine.*;
import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner.ui.PhoneGapRunConfigurationEditor.*;

/**
 * PhoneGapRunConfiguration.java
 * <p/>
 * Created by Masahiro Suzuka on 2014/04/05.
 */
public class PhoneGapRunConfiguration extends LocatableConfigurationBase implements RunProfileWithCompileBeforeLaunchOption {

  private static final String CORDOVA_IOS_LOG_PATH = "/platforms/ios/cordova/console.log";

  private static final Set<String> MAC_SPEC_PLATFORMS = ContainerUtil.immutableSet(PLATFORM_IOS,
                                                                                   PLATFORM_AMAZON_FIREOS,
                                                                                   PLATFORM_ANDROID,
                                                                                   PLATFORM_BLACKBERRY_10,
                                                                                   PLATFORM_BROWSER,
                                                                                   PLATFORM_FIREFOXOS);

  private static final Set<String> WIN_SPEC_PLATFORMS = ContainerUtil.immutableSet(PLATFORM_AMAZON_FIREOS,
                                                                                   PLATFORM_ANDROID,
                                                                                   PLATFORM_BLACKBERRY_10,
                                                                                   PLATFORM_FIREFOXOS,
                                                                                   PLATFORM_WP_8,
                                                                                   PLATFORM_WINDOWS,
                                                                                   PLATFORM_BROWSER,
                                                                                   PLATFORM_WINDOWS_8);

  private static final Set<String> LINUX_SPEC_PLATFORMS = ContainerUtil.immutableSet(PLATFORM_AMAZON_FIREOS,
                                                                                     PLATFORM_ANDROID,
                                                                                     PLATFORM_BROWSER,
                                                                                     PLATFORM_FIREFOXOS,
                                                                                     PLATFORM_UBUNTU);

  private static final Set<String> REMOTE_BUILD_PLATFORMS = ContainerUtil.immutableSet(PLATFORM_IOS,
                                                                                       PLATFORM_ANDROID,
                                                                                       PLATFORM_WP_8);
  public static final String ANDROID_HOME_VARIABLE = "ANDROID_HOME";

  //public for serializer
  @Nullable
  public String myExecutable;

  @Nullable
  public String myWorkDir;

  @Nullable
  public String myCommand;

  public boolean myPassParent = true;

  @NotNull
  public Map<String, String> myEnvs = new LinkedHashMap<>();

  public boolean isPassParent() {
    return myPassParent;
  }

  public void setPassParent(boolean passParent) {
    myPassParent = passParent;
  }

  @NotNull
  public Map<String, String> getEnvs() {
    return myEnvs;
  }

  public void setEnvs(@NotNull Map<String, String> envs) {
    myEnvs = envs;
  }

  @NlsSafe
  @Nullable
  public String getCommand() {
    return myCommand;
  }

  @NlsSafe
  @Nullable
  public String myPlatform;
  
  public String getExtraArgs() {
    return myExtraArgs;
  }

  public void setExtraArgs(String extraArgs) {
    myExtraArgs = extraArgs;
  }

  public String myExtraArgs;

  public boolean hasTarget() {
    return hasTarget;
  }

  public void setHasTarget(boolean hasTarget) {
    this.hasTarget = hasTarget;
  }

  @Nullable
  public String getTarget() {
    return target;
  }

  public void setTarget(@Nullable String target) {
    this.target = target;
  }

  public boolean hasTarget;

  @Nullable
  public String target;

  private volatile PhoneGapCommandLine myCommandLine;

  @Nullable
  public String getWorkDir() {
    return myWorkDir;
  }

  public void setWorkDir(@Nullable String workDir) {
    this.myWorkDir = workDir;
  }

  @Nullable
  public String getExecutable() {
    return myExecutable;
  }

  public void setExecutable(@Nullable String executable) {
    myExecutable = executable;
  }

  public void setCommand(@Nullable String myCommand) {
    this.myCommand = myCommand;
  }

  @NlsSafe
  @Nullable
  public String getPlatform() {
    return myPlatform;
  }

  @NlsSafe
  @Nullable
  public String getNormalizedPlatform() {
    return StringUtil.toLowerCase(getPlatform());
  }

  public void setPlatform(@Nullable @NlsSafe String myPlatform) {
    this.myPlatform = myPlatform;
  }


  public PhoneGapRunConfiguration(Project project, ConfigurationFactory factory, String name) {
    super(project, factory, name);

    //defaults
  }

  @Override
  public String suggestedName() {
    return PhoneGapBundle.message("phonegap.run.default.label");
  }

  @Override
  public void readExternal(@NotNull Element element) throws InvalidDataException {
    super.readExternal(element);

    //noinspection deprecation
    DefaultJDOMExternalizer.readExternal(this, element);
  }

  @Override
  public void writeExternal(@NotNull Element element) throws WriteExternalException {
    super.writeExternal(element);

    //noinspection deprecation
    DefaultJDOMExternalizer.writeExternal(this, element);
  }

  @NotNull
  @Override
  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new PhoneGapRunConfigurationEditor(getProject());
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    if (StringUtil.isEmpty(myCommand)) {
      throw new RuntimeConfigurationError(PhoneGapBundle.message("command.is.missing"));
    }

    if (StringUtil.isEmpty(myPlatform)) {
      throw new RuntimeConfigurationError(PhoneGapBundle.message("platform.is.missing"));
    }

    if (StringUtil.isEmpty(myExecutable)) {
      throw new RuntimeConfigurationError(PhoneGapBundle.message("executable.is.missing"));
    }

    if (StringUtil.isEmpty(myWorkDir)) {
      throw new RuntimeConfigurationError(PhoneGapBundle.message("working.directory.is.missing"));
    }

    if (SystemInfo.isMac && !MAC_SPEC_PLATFORMS.contains(myPlatform)) {
      throwOSWarning();
    }

    if (SystemInfo.isLinux && !LINUX_SPEC_PLATFORMS.contains(myPlatform)) {
      throwOSWarning();
    }

    if (SystemInfo.isWindows && !WIN_SPEC_PLATFORMS.contains(myPlatform)) {
      throwOSWarning();
    }

    if (myPlatform.equals(PLATFORM_FIREFOXOS) &&
        (myCommand.equals(COMMAND_EMULATE) || myCommand.equals(COMMAND_RUN))) {
      throwUnsupportedCommandWarning();
    }

    if (!REMOTE_BUILD_PLATFORMS.contains(myPlatform) &&
        (myCommand.equals(COMMAND_REMOTE_BUILD) || myCommand.equals(COMMAND_REMOTE_RUN))) {
      throwUnsupportedCommandWarning();
    }

    if (myPlatform.equals(PLATFORM_ANDROID) && StringUtil.isEmpty(EnvironmentUtil.getValue(ANDROID_HOME_VARIABLE))) {
      checkExistsSdkWithWarning(PhoneGapAndroidTargets.getAndroidName(), PhoneGapBundle.message("cannot.detect.android.sdk.in.path"));
    }
    if (myPlatform.equals(PLATFORM_IOS)) {
      checkExistsSdkWithWarning(List.of(PhoneGapIosTargets.getIosSimName(),
                                        PhoneGapIosTargets.getIosDeployName()),
                                PhoneGapBundle.message("cannot.detect.ios.sim.and.ios.deploy.in.path"));
    }
  }

  public void throwOSWarning() throws RuntimeConfigurationWarning {
    throw new RuntimeConfigurationWarning(
      PhoneGapBundle.message("dialog.message.applications.for.platform.can.be.built.on.this.os", myPlatform));
  }

  public void throwUnsupportedCommandWarning() throws RuntimeConfigurationWarning {
    throw new RuntimeConfigurationWarning(PhoneGapBundle.message("dialog.message.phonegap.doesn.t.support.for", myCommand, myPlatform));
  }

  public PhoneGapCommandLine getCommandLine() {
    PhoneGapCommandLine current = myCommandLine;
    String executable = getExecutable();
    String workDir = getWorkDir();
    boolean passParentEnv = myPassParent;
    Map<String, String> env = myEnvs;
    if (current != null && StringUtil.equals(current.getPath(), executable) &&
        StringUtil.equals(current.getWorkDir(), workDir) && passParentEnv == current.isPassParentEnv() &&
        env.equals(current.getEnv())) {
      return current;
    }

    assert executable != null;
    assert workDir != null;

    current = new PhoneGapCommandLine(executable, workDir, passParentEnv, env);


    myCommandLine = current;

    return current;
  }

  @SuppressWarnings("MethodDoesntCallSuperMethod")
  @Override
  public PhoneGapRunConfiguration clone() {
    final Element element = new Element("toClone");
    try {
      writeExternal(element);
      PhoneGapRunConfiguration configuration =
        (PhoneGapRunConfiguration)getFactory().createTemplateConfiguration(getProject());
      configuration.setName(getName());
      configuration.readExternal(element);
      return configuration;
    }
    catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Nullable
  @Override
  public RunProfileState getState(@NotNull Executor executor,
                                  @NotNull ExecutionEnvironment executionEnvironment) throws ExecutionException {

    return new PhoneGapRunProfileState(getProject(), executionEnvironment, this);
  }

  private static void checkExistsSdkWithWarning(@Nullable String path, @NotNull @Nls String error) throws RuntimeConfigurationWarning {
    if (path == null) return;

    File file = PathEnvironmentVariableUtil.findInPath(path);
    if (file != null && file.exists()) {
      return;
    }

    throw new RuntimeConfigurationWarning(error);
  }

  private static void checkExistsSdkWithWarning(@Nullable List<String> paths, @NotNull @Nls String error) throws RuntimeConfigurationWarning {
    if (paths == null) return;
    for (String path : paths) {
      File file = PathEnvironmentVariableUtil.findInPath(path);
      if (file != null && file.exists()) {
        return;
      }
    }

    throw new RuntimeConfigurationWarning(error);
  }

  @Override
  public final void customizeLogConsole(final LogConsole console) {
    LogFilterModel model = console.getFilterModel();
    if (model instanceof DefaultLogFilterModel) {
      ((DefaultLogFilterModel)model).setCheckStandartFilters(false);
    }
  }

  @Override
  public @NotNull ArrayList<LogFileOptions> getAllLogFiles() {
    if (!PLATFORM_IOS.equals(myPlatform) || StringUtil.isEmpty(myWorkDir)) super.getAllLogFiles();
    LogFileOptions options = new LogFileOptions("console.log", getPathToLog(), true, false, true);
    return new ArrayList<>(List.of(options));
  }

  private String getPathToLog() {
    assert myWorkDir != null;
    return FileUtil
      .toCanonicalPath(FileUtil.toSystemIndependentName(StringUtil.trimEnd(myWorkDir, File.separator)) + CORDOVA_IOS_LOG_PATH);
  }

  @Override
  public boolean isBuildBeforeLaunchAddedByDefault() {
    return false;
  }
}
