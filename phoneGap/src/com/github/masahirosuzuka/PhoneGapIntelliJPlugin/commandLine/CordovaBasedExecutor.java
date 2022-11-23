package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapCommandLine.*;

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


  public String @NotNull [] createNewProjectCommands(@NotNull String name, String @Nullable [] options) {
    String[] commands = getNewProjectCommands(name);
    if (options == null) {
      return commands;
    }
    else {
      return ArrayUtil.mergeArrays(commands, options);
    }
  }

  public String @NotNull [] getPlatformAddCommands(@NotNull String platform) {
    return new String[]{myPath, "platform", "add", platform};
  }

  public String @NotNull [] getPluginAddCommands(@NotNull String fqn) {
    return new String[]{myPath, "plugin", "add", fqn};
  }

  public String @NotNull [] getPluginRemoveCommands(@NotNull String fqn) {
    return new String[]{myPath, "plugin", "remove", fqn};
  }

  public String @NotNull [] getPluginListCommands() {
    return new String[]{myPath, "plugin", "list"};
  }

  private String @NotNull [] getServeCommands(String extraArgs) {
    return appendParsedArguments(ContainerUtil.newArrayList(myPath, "serve"), extraArgs);
  }

  public String @NotNull [] getEmulateCommand(@NotNull String platform,
                                              @Nullable String target,
                                              @Nullable String extraArg) {

    if (!StringUtil.isEmpty(target)) {
      target = target.trim();
      String[] withoutExtraArgs = {myPath, "run", "--emulator", "--target=" + target, platform};

      return appendParsedArguments(withoutExtraArgs, extraArg);
    }
    return appendParsedArguments(new String[]{myPath, "run", "--emulator", platform}, extraArg);
  }

  private String @NotNull [] getRunCommands(@NotNull String platform,
                                            @Nullable String target,
                                            @Nullable String extraArg) {
    return getStandardCommands(platform, target, extraArg, COMMAND_RUN);
  }


  private String @NotNull [] getPrepareCommands(@NotNull String platform,
                                                @Nullable String target,
                                                @Nullable String extraArg) {
    return getStandardCommands(platform, target, extraArg, COMMAND_PREPARE);
  }

  protected String @NotNull [] getStandardCommands(@NotNull String platform,
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

  protected final String @NotNull [] appendParsedArguments(@NotNull Collection<String> commands, @Nullable String extraArgs) {
    return ArrayUtil.mergeCollections(commands, parseArgs(extraArgs), String[]::new);
  }

  protected final String @NotNull [] appendParsedArguments(String @NotNull [] commands, @Nullable String extraArgs) {
    return ArrayUtil.mergeArrayAndCollection(commands, parseArgs(extraArgs), String[]::new);
  }

  protected String @NotNull [] getNewProjectCommands(@NotNull String name) {
    return new String[]{"create", name};
  }

  @NotNull
  public String getFrameworkName() {
    return "Cordova";
  }
}
