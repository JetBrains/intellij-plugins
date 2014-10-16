package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.apache.commons.codec.Charsets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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
  public static final String COMMAND_EMULATE = "emulate";
  public static final String COMMAND_SERVE = "serve";
  public static final String COMMAND_REMOTE_RUN = "remote run";
  public static final String COMMAND_REMOTE_BUILD = "remote build";

  @Nullable
  private final String myWorkDir;

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

  public static final Function<String, String> REMOVE_QUOTE_AND_TRIM = new Function<String, String>() {
    @Override
    public String fun(String s) {
      return s.replace("'", "").trim();
    }
  };

  public PhoneGapCommandLine(@NotNull String path, @Nullable String dir) {
    myWorkDir = dir;
    myPath = path;
    try {
      version = getInnerVersion(myPath, "--version").replace("\"", "").trim();
    }
    catch (Exception e) {
      version = null;
      LOGGER.debug(e.getMessage(), e);
      myIsCorrect = false;
    }
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
    return KillableColoredProcessHandler.create(commandLine);
  }

  @NotNull
  public OSProcessHandler runCommand(@NotNull String command,
                                     @NotNull String platform,
                                     @Nullable String target,
                                     @Nullable String extraArgs

  ) throws ExecutionException {
    if (COMMAND_RUN.equals(command)) {
      return run(platform, target, extraArgs);
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

  private OSProcessHandler run(@NotNull String platform,
                               @Nullable String target,
                               @Nullable String extraArg) throws ExecutionException {
    String[] command;

    if (!StringUtil.isEmpty(target)) {
      target = target.trim();
      command = new String[]{myPath, "run", "--target=" + target, platform};
    }
    else {
      command = new String[]{myPath, "run", platform};
    }
    return createProcessHandler(concat(newArrayList(command), parseArgs(extraArg)));
  }

  public boolean needAddPlatform() {
    return !isPhoneGap();
  }

  public void createNewProject(String name) throws Exception {
    executeVoidCommand(myPath, (isIonic() ? "start" : "create"), name);
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
    assert myWorkDir != null;
    File file = new File(myPath);
    if (file.getName().contains(PLATFORM_IONIC)) return true;
    if (file.getName().contains(PLATFORM_PHONEGAP)) return false;
    if (file.getName().contains(PLATFORM_CORDOVA)) return false;

    String s = executeAndReturnResult(myPath);

    return s.contains(PLATFORM_IONIC);
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
    try {
      ProcessOutput output = executeAndGetOut(command);

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
    final GeneralCommandLine commandLine = new GeneralCommandLine(command);
    commandLine.withWorkDirectory(myWorkDir);
    commandLine.setPassParentEnvironment(true);
    Process process = commandLine.createProcess();
    OSProcessHandler processHandler = new ColoredProcessHandler(process, commandLine.getCommandLineString(), Charsets.UTF_8);
    final ProcessOutput output = new ProcessOutput();
    processHandler.addProcessListener(new ProcessAdapter() {
      @Override
      public void onTextAvailable(ProcessEvent event, Key outputType) {
        if (outputType == ProcessOutputTypes.STDERR) {
          output.appendStderr(event.getText());
        }
        else if (outputType != ProcessOutputTypes.SYSTEM) {
          output.appendStdout(event.getText());
        }
      }
    });
    processHandler.startNotify();
    if (processHandler.waitFor(TimeUnit.SECONDS.toMillis(120))) {
      output.setExitCode(process.exitValue());
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
    return KillableColoredProcessHandler.create(commandLine);
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
