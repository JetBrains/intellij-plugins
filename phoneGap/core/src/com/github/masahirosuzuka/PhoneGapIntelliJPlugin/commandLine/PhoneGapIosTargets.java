// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine;

import com.intellij.openapi.project.Project;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner.ui.PhoneGapRunConfigurationEditor.PLATFORM_IOS;

public class PhoneGapIosTargets extends PhoneGapTargets {

  private static final Function<String, String> PARSER_DEVICE_LINE = s -> parseDevice(s);

  private static final Function<String, String> PARSER_VIRTUAL_DEVICE_LINE = s -> parseVirtualDevice(s);

  private static final String IOS_SIM = "ios-sim";
  private static final String IOS_DEPLOY = "ios-deploy";
  private static final String EMULATOR_PREFIX = "com.apple.CoreSimulator.SimDeviceType.";

  public PhoneGapIosTargets(@NotNull Project project) {
    super(project);
  }

  @Override
  protected @NotNull List<String> listDevicesNonCached() {
    return list(IOS_DEPLOY, PARSER_DEVICE_LINE, true, "-c", "-t", "1");
  }

  @Override
  protected @NotNull List<String> listVirtualDevicesNonCached() {
    return list(IOS_SIM, PARSER_VIRTUAL_DEVICE_LINE, true, "showdevicetypes");
  }

  @Override
  public @NotNull String platform() {
    return PLATFORM_IOS;
  }

  public static String getIosSimName() {
    return IOS_SIM;
  }

  public static String getIosDeployName() {
    return IOS_DEPLOY;
  }

  /**
   * device id is a string between ()
   * like 2e7c1f7dbedfe54fc7ded3451974f8007e177639 in
   * 'Found iPhone 4S 'Andrey's iPhone' (2e7c1f7dbedfe54fc7ded3451974f8007e177639) connected through USB.'
   */
  private static @Nullable String parseDevice(@Nullable String inputLine) {
    if (inputLine == null) return null;

    int start = inputLine.indexOf('(');
    if (start < 0) return null;

    int end = inputLine.indexOf(')');
    if (end <= 0 || end < start) return null;

    return inputLine.substring(start + 1, end).trim();
  }

  public static String parseVirtualDevice(@Nullable String inputLine) {
    if (inputLine == null) return null;

    String[] splitValues = inputLine.split(",");

    if (splitValues[0].startsWith(EMULATOR_PREFIX)) {
      return splitValues[0].substring(EMULATOR_PREFIX.length()).trim();
    }

    return null;
  }
}
