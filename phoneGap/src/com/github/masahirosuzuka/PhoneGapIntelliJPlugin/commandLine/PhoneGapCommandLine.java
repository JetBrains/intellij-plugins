package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.GeneralCommandLine.ParentEnvironmentType;
import com.intellij.execution.process.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.apache.commons.codec.Charsets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.intellij.openapi.util.text.StringUtil.contains;
import static com.intellij.util.containers.ContainerUtil.concat;
import static com.intellij.util.containers.ContainerUtil.newArrayList;

public class PhoneGapCommandLine {
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
  private String version;
  private boolean myIsCorrect = true;

  public void setPassParentEnv(boolean passParentEnv) {
    myPassParentEnv = passParentEnv;
  }

  public void setEnv(Map<String, String> env) {
    myEnv = env;
  }

  private boolean myPassParentEnv = true;
  private Map<String, String> myEnv = ContainerUtil.newHashMap();
  private final String myOptions;
  public static final Function<String, String> REMOVE_QUOTE_AND_TRIM = s -> s.replace("'", "").trim();

  public PhoneGapCommandLine(@NotNull String path, @Nullable String dir, boolean isPassEnv, Map<String, String> env) {
    myWorkDir = dir;
    myPath = path;
    myEnv = env;
    myPassParentEnv = isPassEnv;
    myOptions = null;
    try {
      version = getInnerVersion(myPath, "--version").replace("\"", "").trim();
    }
    catch (Exception e) {
      version = null;
      LOGGER.debug(e.getMessage(), e);
      myIsCorrect = false;
    }
  }

  public PhoneGapCommandLine(@NotNull String path, @Nullable String dir, @Nullable String options) {
    myWorkDir = dir;
    myPath = path;
    myOptions = options;
    try {
      version = getInnerVersion(myPath, "--version").replace("\"", "").trim();
    }
    catch (Exception e) {
      version = null;
      LOGGER.debug(e.getMessage(), e);
      myIsCorrect = false;
    }
  }

  public PhoneGapCommandLine(@NotNull String path, @Nullable String dir) {
    this(path, dir, null);
  }

  public ProcessOutput platformAdd(@NotNull String platform) throws ExecutionException {
    String trimmedPlatform = platform.trim();
    ProcessOutput output = executeAndGetOut(new String[]{myPath, "platform", "add", trimmedPlatform});

    String message = "Platform " + trimmedPlatform + " already added";
    if (output.getExitCode() != 0 && (contains(output.getStderr(), message) || contains(output.getStdout(), message))) {
      return new ProcessOutput(0);
    }

    return output;
  }

  public boolean isCorrectExecutable() {
    return myIsCorrect;
  }

  public String version() {
    return version;
  }

  public void pluginAdd(String fqn) {
    executeVoidCommand(myPath, "plugin", "add", fqn);
  }

  public void pluginRemove(String fqn) {
    executeVoidCommand(myPath, "plugin", "remove", fqn);
  }

  public ProcessOutput pluginListRaw() throws ExecutionException {
    return executeAndGetOut(new String[]{myPath, "plugin", "list"});
  }

  public String getPlatformName() {
    return isIonic() ? "Ionic" : "PhoneGap/Cordova";
  }

  public boolean isOld() {
    if (isIonic()) return false;

    if (StringUtil.isEmpty(version) || !Character.isDigit(version.charAt(0))) return false;

    try {
      String[] split = version.split("\\.");
      int first = Integer.parseInt(split[0]);
      if (first > 3) return false;
      if (first == 3) {
        return (split.length < 2 || Integer.parseInt(split[1]) < 5);
      }
      return first < 3;
    }
    catch (RuntimeException e) {
      LOGGER.debug(e.getMessage(), e);
    }
    return false;
  }

  @NotNull
  public List<String> pluginList() {
    try {
      String out = executeAndReturnResult(myPath, "plugin", "list").trim();
      return parsePluginList(out);
    }
    catch (RuntimeException e) {
      LOGGER.debug(e.getMessage(), e);
      return Collections.emptyList();
    }
  }

  private OSProcessHandler serve(String extraArg) throws ExecutionException {
    GeneralCommandLine commandLine = new GeneralCommandLine(concat(newArrayList(myPath, "serve"), parseArgs(extraArg)));
    commandLine.withWorkDirectory(myWorkDir);
    return new KillableColoredProcessHandler(commandLine, true);
  }

  @NotNull
  public OSProcessHandler runCommand(@NotNull String command,
                                     @NotNull String platform,
                                     @Nullable String target,
                                     @Nullable String extraArgs

  ) throws ExecutionException {
    if (COMMAND_RUN.equals(command)) {
      return executeStandardCommand(platform, target, extraArgs, COMMAND_RUN);
    }

    if (COMMAND_EMULATE.equals(command)) {
      return emulate(platform, target, extraArgs);
    }

    if (COMMAND_SERVE.equals(command)) {
      return serve(extraArgs);
    }

    if (COMMAND_REMOTE_RUN.equals(command)) {
      return remoteRun(platform, extraArgs);
    }

    if (COMMAND_REMOTE_BUILD.equals(command)) {
      return remoteBuild(platform, extraArgs);
    }

    if (COMMAND_PREPARE.equals(command)) {
      return executeStandardCommand(platform, target, extraArgs, COMMAND_PREPARE);
    }

    throw new IllegalStateException("Unsupported command");
  }

  private OSProcessHandler remoteRun(@NotNull String platform, @Nullable String extraArg) throws ExecutionException {
    return createProcessHandler(concat(newArrayList(myPath, "remote", "run", platform), parseArgs(extraArg)));
  }

  private OSProcessHandler remoteBuild(@NotNull String platform, @Nullable String extraArg) throws ExecutionException {

    return createProcessHandler(concat(newArrayList(myPath, "remote", "build", platform), parseArgs(extraArg)));
  }

  private OSProcessHandler emulate(@NotNull String platform,
                                   @Nullable String target,
                                   @Nullable String extraArg) throws ExecutionException {
    String[] command;

    if (!StringUtil.isEmpty(target)) {
      target = target.trim();
      command = new String[]{myPath, "run", "--emulator", "--target=" + target, platform};
    }
    else {
      command = new String[]{myPath, "run", "--emulator", platform};
    }
    return createProcessHandler(concat(newArrayList(command), parseArgs(extraArg)));
  }

  private OSProcessHandler executeStandardCommand(@NotNull String platform,
                                                  @Nullable String target,
                                                  @Nullable String extraArg,
                                                  @NotNull String commandToExecute) throws ExecutionException {
    String[] command;

    if (!StringUtil.isEmpty(target)) {
      target = target.trim();
      command = new String[]{myPath, commandToExecute, "--target=" + target, platform};
    }
    else {
      command = new String[]{myPath, commandToExecute, platform};
    }
    return createProcessHandler(concat(newArrayList(command), parseArgs(extraArg)));
  }

  public boolean needAddPlatform() {
    if (!isPhoneGap()) return true;

    return isPhonegapAfter363(version);
  }

  static boolean isPhonegapAfter363(String version) {
    if (StringUtil.isEmpty(version)) return true;

    return StringUtil.compareVersionNumbers(version, "3.6.3") >= 0;
  }

  public void createNewProject(String name, @Nullable ProgressIndicator indicator) throws Exception {
    String command = isIonic() ? "start" : "create";
    if (myOptions == null) {
      executeVoidCommand(indicator, myPath, command, name);
    }
    else {
      String[] resultCommand = ArrayUtil.mergeArrays(new String[]{myPath, command, name, myOptions});
      executeVoidCommand(indicator, resultCommand);
    }
  }

  private boolean isPhoneGap() {
    assert myWorkDir != null;
    Boolean isPhoneGapByName = isPhoneGapExecutableByPath(myPath);
    if (isPhoneGapByName != null) return isPhoneGapByName;

    String s = executeAndReturnResult(myPath);

    return s.contains(PLATFORM_PHONEGAP);
  }

  /**
   * @param path
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
    File file = new File(myPath);
    if (file.getName().contains(PLATFORM_IONIC)) return true;
    if (file.getName().contains(PLATFORM_PHONEGAP)) return false;
    if (file.getName().contains(PLATFORM_CORDOVA)) return false;

    if (myWorkDir != null) {
      String s = executeAndReturnResult(myPath);
      return s.contains(PLATFORM_IONIC);
    }

    return false;
  }

  static List<String> parsePluginList(String out) {
    if (StringUtil.isEmpty(out) || contains(out.toLowerCase(Locale.getDefault()), "no plugins")) {
      return newArrayList();
    }

    if (out.startsWith("[") && out.endsWith("]")) {
      out = out.substring(1, out.length() - 1);
      return ContainerUtil.map(out.split(","), REMOVE_QUOTE_AND_TRIM);
    }

    if (out.startsWith("[")) {
      out = out.replaceAll("\\[(.*?)\\]", "");
    }
    List<String> plugins = ContainerUtil.map(out.split("\n"), StringUtil.TRIMMER);
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
      ProcessOutput output = executeAndGetOut(indicator, command);

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
      final ProcessOutput output = executeAndGetOut(command);

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

  private String executeAndReturnResult(String... command) {
    try {
      final ProcessOutput output = executeAndGetOut(command);

      if (output.getExitCode() > 0) {
        throw new RuntimeException("Command error: " + output.getStderr());
      }

      return output.getStdout();
    }
    catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  private ProcessOutput executeAndGetOut(String[] command) throws ExecutionException {
    return executeAndGetOut(null, command);
  }

  private ProcessOutput executeAndGetOut(@Nullable ProgressIndicator indicator, String[] command) throws ExecutionException {
    final GeneralCommandLine commandLine = new GeneralCommandLine(command);
    commandLine.withWorkDirectory(myWorkDir);
    commandLine.withParentEnvironmentType(myPassParentEnv ? ParentEnvironmentType.CONSOLE : ParentEnvironmentType.NONE);
    commandLine.withEnvironment(myEnv);
    commandLine.setCharset(Charsets.UTF_8);

    OSProcessHandler processHandler = new ColoredProcessHandler(commandLine);
    final ProcessOutput output = new ProcessOutput();
    processHandler.addProcessListener(new ProcessAdapter() {
      @Override
      public void onTextAvailable(ProcessEvent event, Key outputType) {
        if (indicator != null ) {
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
    if (processHandler.waitFor(PROCESS_TIMEOUT)) {
      output.setExitCode(processHandler.getProcess().exitValue());
    }
    else {
      processHandler.destroyProcess();
      output.setTimeout();
    }
    return output;
  }

  private OSProcessHandler createProcessHandler(List<String> commands) throws ExecutionException {
    return createProcessHandler(ArrayUtil.toStringArray(commands));
  }

  private OSProcessHandler createProcessHandler(String... commands) throws ExecutionException {
    GeneralCommandLine commandLine = new GeneralCommandLine(commands);
    commandLine.withWorkDirectory(myWorkDir);
    return new KillableColoredProcessHandler(commandLine, true);
  }

  private static List<String> parseArgs(String paramList) {
    ArrayList<String> list = newArrayList();

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
