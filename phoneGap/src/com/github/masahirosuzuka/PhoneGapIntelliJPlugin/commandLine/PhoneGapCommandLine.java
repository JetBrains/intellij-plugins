// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.GeneralCommandLine.ParentEnvironmentType;
import com.intellij.execution.process.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Function;
import com.intellij.util.ThreeState;
import com.intellij.util.containers.ContainerUtil;
import org.intellij.lang.regexp.AsciiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.intellij.openapi.util.text.StringUtil.contains;

public final class PhoneGapCommandLine {
  private static final Logger LOGGER = Logger.getInstance(PhoneGapCommandLine.class);

  public static final String INFO_PHONEGAP = "the following plugins are installed";

  public static final String PLATFORM_PHONEGAP = "phonegap";
  public static final String PLATFORM_CORDOVA = "cordova";
  public static final String PLATFORM_IONIC = "ionic";
  public static final String COMMAND_RUN = "run";
  public static final String COMMAND_PREPARE = "prepare";
  public static final String COMMAND_EMULATE = "emulate";
  public static final String COMMAND_SERVE = "serve";
  public static final String COMMAND_REMOTE_RUN = "remote run";
  public static final String COMMAND_REMOTE_BUILD = "remote build";
  public static final long PROCESS_TIMEOUT = TimeUnit.SECONDS.toMillis(120);
  public static final long PROCESS_VERSION_TIMEOUT = TimeUnit.MILLISECONDS.toMillis(10);

  @Nullable
  private final String myWorkDir;

  public boolean isPassParentEnv() {
    return myPassParentEnv;
  }

  public Map<String, String> getEnv() {
    return myEnv;
  }

  @Nullable
  public String getWorkDir() {

    return myWorkDir;
  }

  @NotNull
  public String getPath() {
    return myPath;
  }

  @NotNull
  private final String myPath;

  @Nullable
  private String myVersion;
  private boolean myIsCorrect = true;


  private final boolean myPassParentEnv;
  private final Map<String, String> myEnv;
  public static final Function<String, String> REMOVE_QUOTE_AND_TRIM = s -> s.replace("'", "").trim();

  public PhoneGapCommandLine(@NotNull String path, @Nullable String dir, boolean isPassEnv, Map<String, String> env) {
    myWorkDir = dir;
    myPath = path;
    myEnv = env;
    myPassParentEnv = isPassEnv;
    try {
      String version = isIonicPath(path) == ThreeState.YES ?
                       getInnerVersion(myPath, "--version", "--no-interactive") :
                       getInnerVersion(myPath, "--version");

      myVersion = version.replace("\"", "").trim();
    }
    catch (Exception e) {
      myVersion = null;
      LOGGER.debug(e.getMessage(), e);
      myIsCorrect = false;
    }
  }

  public PhoneGapCommandLine(@NotNull String path, @Nullable String dir) {
    this(path, dir, true, new HashMap<>());
  }

  @NotNull
  public CapturingProcessHandler platformAdd(@NotNull String platform) throws ExecutionException {
    platform = platform.trim();
    String[] command = getExecutor().getPlatformAddCommands(platform);
    return createCapturingProcessHandler(command);
  }

  private CordovaBasedExecutor getExecutor() {
    if (isIonic()) return new IonicExecutor(myPath, myVersion);
    if (isPhoneGap()) return new PhoneGapExecutor(myPath, myVersion);

    return new CordovaBasedExecutor(myPath, myVersion);
  }

  public boolean isCorrectExecutable() {
    return myIsCorrect;
  }

  @NlsSafe
  public String version() {
    return myVersion;
  }

  public void pluginAdd(String fqn) {
    executeVoidCommand(getExecutor().getPluginAddCommands(fqn));
  }

  public void pluginRemove(String fqn) {
    executeVoidCommand(getExecutor().getPluginRemoveCommands(fqn));
  }

  public ProcessOutput pluginListRaw() throws ExecutionException {
    return executeAndGetOut(getExecutor().getPluginListCommands());
  }

  public String getPlatformName() {
    return getExecutor().getFrameworkName();
  }

  public boolean isOld() {
    if (isIonic()) return false;

    if (StringUtil.isEmpty(myVersion) || !Character.isDigit(myVersion.charAt(0))) return false;

    try {
      String[] split = myVersion.split("\\.");
      int first = Integer.parseInt(split[0]);
      if (first > 3) return false;
      if (first == 3) {
        return (split.length < 2 || Integer.parseInt(split[1]) < 5);
      }
      return true;
    }
    catch (RuntimeException e) {
      LOGGER.debug(e.getMessage(), e);
    }
    return false;
  }

  @NotNull
  public List<String> pluginList() {
    try {
      String out = executeAndReturnResult(getExecutor().getPluginListCommands()).trim();
      return parsePluginList(out);
    }
    catch (RuntimeException e) {
      LOGGER.debug(e.getMessage(), e);
      return Collections.emptyList();
    }
  }


  @NotNull
  public OSProcessHandler runCommand(@NotNull String command,
                                     @NotNull String platform,
                                     @Nullable String target,
                                     @Nullable String extraArgs) throws ExecutionException {
    return createProcessHandler(getExecutor().getCommands(command, platform, target, extraArgs));
  }


  public boolean needAddPlatform() {
    if (!isPhoneGap()) return true;

    return PhoneGapExecutor.isPhoneGapAfter363(myVersion);
  }


  public void createNewProject(String name, @Nullable ProgressIndicator indicator) {
    executeVoidCommand(indicator, getExecutor().createNewProjectCommands(name, null));
  }

  public String @NotNull [] getCreateNewProjectCommand(String name) {
    return getExecutor().createNewProjectCommands(name, null);
  }

  private boolean isPhoneGap() {
    assert myWorkDir != null;
    Boolean isPhoneGapByName = isPhoneGapExecutableByPath(myPath);
    if (isPhoneGapByName != null) return isPhoneGapByName;

    String s = executeAndReturnResult(PROCESS_VERSION_TIMEOUT, myPath);

    return s.contains(PLATFORM_PHONEGAP);
  }

  /**
   * @return true - phonegap / false - not phonegap / null - cannot detect
   */
  @Nullable
  public static Boolean isPhoneGapExecutableByPath(@Nullable String path) {
    if (StringUtil.isEmpty(path)) return false;

    File file = new File(path);
    if (!file.exists()) return false;
    if (file.getName().contains(PLATFORM_IONIC)) return false;
    if (file.getName().contains(PLATFORM_CORDOVA)) return false;
    if (file.getName().contains(PLATFORM_PHONEGAP)) return true;

    return null;
  }


  private boolean isIonic() {
    ThreeState isIonic = isIonicPath(myPath);
    if (isIonic != ThreeState.UNSURE) {
      return isIonic.toBoolean();
    }

    if (myWorkDir != null) {
      String s = executeAndReturnResult(PROCESS_VERSION_TIMEOUT, myPath);
      return s.contains(PLATFORM_IONIC);
    }

    return false;
  }


  @NotNull
  public static ThreeState isIonicPath(@Nullable String path) {
    if (StringUtil.isEmptyOrSpaces(path)) return ThreeState.NO;

    File file = new File(path);
    String name = file.getName();
    if (name.contains(PLATFORM_IONIC)) return ThreeState.YES;
    if (name.contains(PLATFORM_PHONEGAP)) return ThreeState.NO;
    if (name.contains(PLATFORM_CORDOVA)) return ThreeState.NO;

    return ThreeState.UNSURE;
  }

  static List<String> parsePluginList(String out) {
    if (StringUtil.isEmpty(out) || contains(out.toLowerCase(Locale.getDefault()), "no plugins")) {
      return new ArrayList<>();
    }

    if (out.startsWith("[") && out.endsWith("]")) {
      out = out.substring(1, out.length() - 1);
      return ContainerUtil.map(out.split(","), REMOVE_QUOTE_AND_TRIM);
    }

    if (out.startsWith("[")) {
      out = out.replaceAll("\\[(.*?)\\]", "");
    }
    List<String> plugins = Arrays.stream(out.split("\n"))
      .map(StringUtil::trim)
      .filter(el -> el.length() > 0 && AsciiUtil.isLetter(el.charAt(0)))
      .collect(Collectors.toList());

    String item = ContainerUtil.getFirstItem(plugins);
    if (item != null && item.contains(INFO_PHONEGAP)) {
      plugins = plugins.subList(1, plugins.size());
    }
    return plugins;
  }

  private void executeVoidCommand(final String... command) {
    executeVoidCommand(null, command);
  }

  private void executeVoidCommand(ProgressIndicator indicator, final String... command) {
    try {
      ProcessOutput output = executeAndGetOut(PROCESS_TIMEOUT, indicator, command);

      if (output.getExitCode() > 0) {
        throw new RuntimeException("Command error: " + output.getStderr());
      }
    }
    catch (Exception e) {
      LOGGER.debug(e.getMessage(), e);

      throw new RuntimeException("Select correct executable path", e);
    }
  }

  private String getInnerVersion(String... command) {
    try {
      final ProcessOutput output = executeAndGetOut(PROCESS_VERSION_TIMEOUT, null, command);

      String stderr = output.getStderr();
      if (output.getExitCode() > 0) {
        throw new RuntimeException("Command error: " + stderr);
      }

      String stdout = output.getStdout();
      if (StringUtil.isEmpty(stdout) && !StringUtil.isEmpty(stderr)) {
        return stderr;
      }

      return stdout;
    }
    catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  private String executeAndReturnResult(long timeout, String... command) {
    try {
      final ProcessOutput output = executeAndGetOut(timeout, null, command);

      if (output.getExitCode() > 0) {
        throw new RuntimeException("Command error: " + output.getStderr());
      }

      return output.getStdout();
    }
    catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  private String executeAndReturnResult(String... command) {
    return executeAndReturnResult(PROCESS_TIMEOUT, command);
  }

  private ProcessOutput executeAndGetOut(String[] command) throws ExecutionException {
    return executeAndGetOut(PROCESS_TIMEOUT, null, command);
  }

  private ProcessOutput executeAndGetOut(long timeout, @Nullable ProgressIndicator indicator, String[] command) throws ExecutionException {
    final GeneralCommandLine commandLine = new GeneralCommandLine(command);
    commandLine.withWorkDirectory(myWorkDir);
    commandLine.withParentEnvironmentType(myPassParentEnv ? ParentEnvironmentType.CONSOLE : ParentEnvironmentType.NONE);
    commandLine.withEnvironment(myEnv);
    commandLine.setCharset(StandardCharsets.UTF_8);

    OSProcessHandler processHandler = new ColoredProcessHandler(commandLine);
    final ProcessOutput output = new ProcessOutput();
    processHandler.addProcessListener(new ProcessAdapter() {
      @Override
      public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
        if (indicator != null) {
          String s = StringUtil.trim(event.getText());
          if (!StringUtil.isEmpty(s)) {
            indicator.setText2(s);
          }
        }

        if (outputType == ProcessOutputTypes.STDERR) {
          output.appendStderr(event.getText());
        }
        else if (outputType != ProcessOutputTypes.SYSTEM) {
          output.appendStdout(event.getText());
        }
      }
    });
    processHandler.startNotify();
    if (processHandler.waitFor(timeout)) {
      output.setExitCode(processHandler.getProcess().exitValue());
    }
    else {
      processHandler.destroyProcess();
      output.setTimeout();
    }
    return output;
  }

  private OSProcessHandler createProcessHandler(String... commands) throws ExecutionException {
    GeneralCommandLine commandLine = new GeneralCommandLine(commands);
    commandLine.withWorkDirectory(myWorkDir);
    return new KillableColoredProcessHandler(commandLine);
  }

  private CapturingProcessHandler createCapturingProcessHandler(String... commands) throws ExecutionException {
    GeneralCommandLine commandLine = new GeneralCommandLine(commands);
    commandLine.withWorkDirectory(myWorkDir);
    return new CapturingProcessHandler(commandLine);
  }

  public static List<String> parseArgs(String paramList) {
    ArrayList<String> list = new ArrayList<>();

    if (StringUtil.isEmpty(paramList)) return list;

    for (String s : paramList.split(" ")) {
      String trim = StringUtil.trim(s);
      if (trim != null && !trim.isEmpty()) {
        list.add(trim);
      }
    }

    return list;
  }
}
