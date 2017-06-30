package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapCommandLine.*;
import static com.intellij.util.containers.ContainerUtil.newArrayList;

public class CordovaBasedExecutor {

  @Nullable
  protected final String myPath;
  @Nullable
  protected final String myVersion;

  public CordovaBasedExecutor(@Nullable String path, @Nullable String version) {
    myPath = path;
    myVersion = version;
  }


  public String[] getCommands(@NotNull String command,
                              @NotNull String platform,
                              @Nullable String target,
                              @Nullable String extraArgs) {
    if (COMMAND_SERVE.equals(command)) {
      return getServeCommands(extraArgs);
    }

    if (COMMAND_RUN.equals(command)) {
      return getRunCommands(platform, target, extraArgs);
    }

    if (COMMAND_EMULATE.equals(command)) {
      return getEmulateCommand(platform, target, extraArgs);
    }

    if (COMMAND_PREPARE.equals(command)) {
      return getPrepareCommands(platform, target, extraArgs);
    }

    throw new IllegalStateException("Unsupported command");
  }


  @NotNull
  public String[] createNewProjectCommands(@NotNull String name, @Nullable String[] options) {
    String[] commands = getNewProjectCommands(name);
    if (options == null) {
      return commands;
    }
    else {
      return ArrayUtil.mergeArrays(commands, options);
    }
  }

  @NotNull
  public String[] getPlatformAddCommands(@NotNull String platform) {
    return new String[]{myPath, "platform", "add", platform};
  }

  @NotNull
  public String[] getPluginAddCommands(@NotNull String fqn) {
    return new String[]{myPath, "plugin", "add", fqn};
  }

  @NotNull
  public String[] getPluginRemoveCommands(@NotNull String fqn) {
    return new String[]{myPath, "plugin", "remove", fqn};
  }

  @NotNull
  public String[] getPluginListCommands() {
    return new String[]{myPath, "plugin", "list"};
  }

  @NotNull
  private String[] getServeCommands(String extraArgs) {
    return appendParsedArguments(newArrayList(myPath, "serve"), extraArgs);
  }

  @NotNull
  public String[] getEmulateCommand(@NotNull String platform,
                                    @Nullable String target,
                                    @Nullable String extraArg) {

    if (!StringUtil.isEmpty(target)) {
      target = target.trim();
      String[] withoutExtraArgs = {myPath, "run", "--emulator", "--target=" + target, platform};

      return appendParsedArguments(withoutExtraArgs, extraArg);
    }
    return appendParsedArguments(new String[]{myPath, "run", "--emulator", platform}, extraArg);
  }

  @NotNull
  private String[] getRunCommands(@NotNull String platform,
                                  @Nullable String target,
                                  @Nullable String extraArg) {
    return getStandardCommands(platform, target, extraArg, COMMAND_RUN);
  }


  @NotNull
  private String[] getPrepareCommands(@NotNull String platform,
                                      @Nullable String target,
                                      @Nullable String extraArg) {
    return getStandardCommands(platform, target, extraArg, COMMAND_PREPARE);
  }

  @NotNull
  protected String[] getStandardCommands(@NotNull String platform,
                                         @Nullable String target,
                                         @Nullable String extraArg,
                                         @NotNull String commandToExecute) {
    String[] command;
    if (!StringUtil.isEmpty(target)) {
      command = new String[]{myPath, commandToExecute, "--target=" + target.trim(), platform};
    }
    else {
      command = new String[]{myPath, commandToExecute, platform};
    }
    return appendParsedArguments(command, extraArg);
  }

  @NotNull
  protected final String[] appendParsedArguments(@NotNull Collection<String> commands, @Nullable String extraArgs) {
    return ArrayUtil.mergeCollections(commands, parseArgs(extraArgs), String[]::new);
  }

  @NotNull
  protected final String[] appendParsedArguments(@NotNull String[] commands, @Nullable String extraArgs) {
    return ArrayUtil.mergeArrayAndCollection(commands, parseArgs(extraArgs), String[]::new);
  }

  @NotNull
  protected String[] getNewProjectCommands(@NotNull String name) {
    return new String[]{myPath, "create", name};
  }


  @NotNull
  public String getFrameworkName() {
    return "PhoneGap/Cordova";
  }
}
