// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.text.SemVer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IonicExecutor extends CordovaBasedExecutor {

  public IonicExecutor(String path, @Nullable String version) {
    super(path, version);
  }

  @Override
  protected String @NotNull [] getNewProjectCommands(@NotNull String name) {
    if (!isIonic3AndLater()) {
      return new String[]{"start", name};
    }

    return new String[]{"start", name, "blank", "--skip-link", "--confirm", "--no-interactive"};
  }


  @Override
  public @NotNull String getFrameworkName() {
    return "Ionic";
  }

  @Override
  protected String @NotNull [] getStandardCommands(@NotNull String platform,
                                                   @Nullable String target,
                                                   @Nullable String extraArg,
                                                   @NotNull String commandToExecute) {
    if (!isIonic3AndLater()) {
      return super.getStandardCommands(platform, target, extraArg, commandToExecute);
    }

    String[] command;
    if (!StringUtil.isEmpty(target)) {
      command = new String[]{myPath, "cordova", commandToExecute, "--target=" + target.trim(), platform, "--confirm", "--no-interactive"};
    }
    else {
      command = new String[]{myPath, "cordova", commandToExecute, platform, "--confirm", "--no-interactive"};
    }
    return appendParsedArguments(command, extraArg);
  }

  @Override
  public String @NotNull [] getEmulateCommand(@NotNull String platform,
                                              @Nullable String target,
                                              @Nullable String extraArg) {
    if (!isIonic3AndLater()) {
      return super.getEmulateCommand(platform, target, extraArg);
    }

    if (!StringUtil.isEmpty(target)) {
      target = target.trim();
      String[] withoutExtraArgs = {myPath, "cordova", "run", "--confirm", "--no-interactive", "--emulator", "--target=" + target, platform};

      return appendParsedArguments(withoutExtraArgs, extraArg);
    }
    return appendParsedArguments(new String[]{myPath, "cordova", "run", "--confirm", "--no-interactive", "--emulator", platform}, extraArg);
  }

  @Override
  public String @NotNull [] getPlatformAddCommands(@NotNull String platform) {
    if (!isIonic3AndLater()) {
      return super.getPlatformAddCommands(platform);
    }

    return new String[]{myPath, "cordova", "platform", "add", platform, "--confirm", "--no-interactive"};
  }

  @Override
  public String @NotNull [] getPluginAddCommands(@NotNull String fqn) {
    if (!isIonic3AndLater()) {
      return super.getPluginAddCommands(fqn);
    }

    return new String[]{myPath, "cordova", "plugin", "add", fqn, "--confirm", "--no-interactive"};
  }

  @Override
  public String @NotNull [] getPluginRemoveCommands(@NotNull String fqn) {
    if (!isIonic3AndLater()) {
      return super.getPluginRemoveCommands(fqn);
    }

    return new String[]{myPath, "cordova", "plugin", "remove", fqn, "--confirm", "--no-interactive"};
  }

  @Override
  public String @NotNull [] getPluginListCommands() {
    if (!isIonic3AndLater()) {
      return super.getPluginListCommands();
    }

    return new String[]{myPath, "cordova", "plugin", "list", "--confirm", "--no-interactive"};
  }

  private boolean isIonic3AndLater() {
    SemVer parsedVersion = SemVer.parseFromText(myVersion);
    return parsedVersion == null || parsedVersion.getMajor() >= 3;
  }
}
