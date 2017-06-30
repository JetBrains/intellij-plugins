package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.text.SemVer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IonicExecutor extends CordovaBasedExecutor {

  public IonicExecutor(String path, String version) {
    super(path, version);
  }

  @NotNull
  @Override
  protected String[] getNewProjectCommands(@NotNull String name) {
    if (isIonic3AndLater()) {
      return new String[]{myPath, "start", name, "blank", "--skip-link", "--confirm", "--no-interactive"};
    }
    return new String[]{myPath, "start", name};
  }


  @NotNull
  public String getFrameworkName() {
    return "Ionic";
  }

  @NotNull
  @Override
  protected String[] getStandardCommands(@NotNull String platform,
                                         @Nullable String target,
                                         @Nullable String extraArg,
                                         @NotNull String commandToExecute) {
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
  @NotNull
  public String[] getEmulateCommand(@NotNull String platform,
                                    @Nullable String target,
                                    @Nullable String extraArg) {

    if (!StringUtil.isEmpty(target)) {
      target = target.trim();
      String[] withoutExtraArgs = {myPath, "cordova", "run", "--confirm", "--no-interactive", "--emulator", "--target=" + target, platform};

      return appendParsedArguments(withoutExtraArgs, extraArg);
    }
    return appendParsedArguments(new String[]{myPath, "cordova", "run", "--confirm", "--no-interactive", "--emulator", platform}, extraArg);
  }

  @Override
  @NotNull
  public String[] getPlatformAddCommands(@NotNull String platform) {
    return new String[]{myPath, "cordova", "platform", "add", platform, "--confirm", "--no-interactive"};
  }

  @Override
  @NotNull
  public String[] getPluginAddCommands(@NotNull String fqn) {
    return new String[]{myPath, "cordova", "plugin", "add", fqn, "--confirm", "--no-interactive"};
  }

  @Override
  @NotNull
  public String[] getPluginRemoveCommands(@NotNull String fqn) {
    return new String[]{myPath, "cordova", "plugin", "remove", fqn, "--confirm", "--no-interactive"};
  }

  @Override
  @NotNull
  public String[] getPluginListCommands() {
    return new String[]{myPath, "cordova", "plugin", "list", "--confirm", "--no-interactive"};
  }

  private boolean isIonic3AndLater() {
    SemVer parsedVersion = SemVer.parseFromText(myVersion);
    return parsedVersion != null && parsedVersion.getMajor() >= 3;
  }
}
