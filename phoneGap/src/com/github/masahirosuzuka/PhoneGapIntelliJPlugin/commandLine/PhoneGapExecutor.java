package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine;

import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapCommandLine.COMMAND_REMOTE_BUILD;
import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapCommandLine.COMMAND_REMOTE_RUN;

public class PhoneGapExecutor extends CordovaBasedExecutor {
  public PhoneGapExecutor(@Nullable String path, @Nullable String version) {
    super(path, version);
  }

  static boolean isPhoneGapAfter363(String version) {
    if (StringUtil.isEmpty(version)) return true;

    return StringUtil.compareVersionNumbers(version, "3.6.3") >= 0;
  }

  @Override
  public String[] getCommands(@NotNull String command, @NotNull String platform, @Nullable String target, @Nullable String extraArgs) {
    if (COMMAND_REMOTE_RUN.equals(command)) {
      return getRemoteRunCommands(platform, extraArgs);
    }

    if (COMMAND_REMOTE_BUILD.equals(command)) {
      return getRemoteBuildCommands(platform, extraArgs);
    }

    return super.getCommands(command, platform, target, extraArgs);
  }

  private String[] getRemoteRunCommands(@NotNull String platform, @Nullable String extraArg) {
    return appendParsedArguments(Arrays.asList(myPath, "remote", "run", platform), extraArg);
  }

  private String[] getRemoteBuildCommands(@NotNull String platform, @Nullable String extraArg) {
    return appendParsedArguments(Arrays.asList(myPath, "remote", "build", platform), extraArg);
  }
}
