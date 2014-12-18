package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine;

import com.intellij.openapi.project.Project;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner.ui.PhoneGapRunConfigurationEditor.PLATFORM_IOS;

public class PhoneGapIosTargets extends PhoneGapTargets {

  public PhoneGapIosTargets(@NotNull Project project) {
    super(project);
  }

  @NotNull
  @Override
  protected List<String> listDevicesNonCached() {
    return ContainerUtil.emptyList();
  }

  @NotNull
  @Override
  protected List<String> listVirtualDevicesNonCached() {
    return ContainerUtil.emptyList();
  }

  @NotNull
  @Override
  public String platform() {
    return PLATFORM_IOS;
  }

  public static String getIosSimName() {
    return "ios-sim";
  }
}
