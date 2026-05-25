// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine;


import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner.ui.PhoneGapRunConfigurationEditor.PLATFORM_ANDROID;

public class PhoneGapAndroidTargets extends PhoneGapTargets {

  public static final Function<String, String> PARSER_VIRTUAL_DEVICES = s -> {
    if (StringUtil.isEmpty(s)) return null;
    if (isAndroidExcludedStrings(s)) return null;

    return s.trim();
  };

  public static final Function<String, String> PARSER_DEVICES = s -> {
    if (StringUtil.isEmpty(s)) return null;
    if (isAndroidExcludedStrings(s)) return null;

    return s.split("\t")[0].trim();
  };

  public PhoneGapAndroidTargets(@NotNull Project project) {
    super(project);
  }

  public static @NotNull String getAndroidName() {
    return SystemInfo.isWindows ? "android" + ".bat" : "android";
  }

  private static String getAdbName() {
    return SystemInfo.isWindows ? "adb" + ".exe" : "adb";
  }


  private static boolean isAndroidExcludedStrings(String device) {
    //possible output:
    //* daemon not running. starting it now on port 5037 *
    //* daemon started successfully *
    //List of devices attached
    return device.startsWith("List of devices attached") ||
           (device.startsWith("* ") && device.endsWith(" *"));
  }

  @Override
  protected @NotNull List<String> listDevicesNonCached() {
    return list(getAdbName(), PARSER_DEVICES, false, "devices");
  }

  @Override
  protected @NotNull List<String> listVirtualDevicesNonCached() {
    return list(getAndroidName(), PARSER_VIRTUAL_DEVICES, false, "-v", "list", "avd", "-c");
  }

  @Override
  public @NotNull String platform() {
    return PLATFORM_ANDROID;
  }
}
