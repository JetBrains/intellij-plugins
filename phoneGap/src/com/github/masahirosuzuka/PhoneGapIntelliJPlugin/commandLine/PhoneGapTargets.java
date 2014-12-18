package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine;


import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapCommandLine.COMMAND_EMULATE;
import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapCommandLine.COMMAND_RUN;
import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner.ui.PhoneGapRunConfigurationEditor.PLATFORM_ANDROID;
import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner.ui.PhoneGapRunConfigurationEditor.PLATFORM_IOS;

public abstract class PhoneGapTargets {
  @Nullable
  public static PhoneGapTargets createTargetsList(@NotNull Project project, @Nullable String platform) {
    if (PLATFORM_ANDROID.equals(platform)) {
      return new PhoneGapAndroidTargets(project);
    }
    if (PLATFORM_IOS.equals(platform)) {
      return new PhoneGapIosTargets(project);
    }

    return null;
  }

  @NotNull
  public static List<String> listTargets(@Nullable PhoneGapTargets phoneGapTargets, @Nullable String command) {
    if (phoneGapTargets == null) return ContainerUtil.emptyList();

    if (COMMAND_EMULATE.equals(command)) {
      return phoneGapTargets.listVirtualDevices();
    }

    if (COMMAND_RUN.equals(command)) {
      return phoneGapTargets.listDevices();
    }

    return ContainerUtil.emptyList();
  }


  @NotNull
  protected final Project myProject;

  private final NotNullLazyValue<List<String>> myVirtualDevices = new NotNullLazyValue<List<String>>() {
    @NotNull
    @Override
    protected List<String> compute() {
      return listVirtualDevicesNonCached();
    }
  };

  private final NotNullLazyValue<List<String>> myDevices = new NotNullLazyValue<List<String>>() {
    @NotNull
    @Override
    protected List<String> compute() {
      return listDevicesNonCached();
    }
  };

  public PhoneGapTargets(@NotNull Project project) {
    myProject = project;
  }

  @NotNull
  public List<String> listDevices() {
    return myDevices.getValue();
  }

  @NotNull
  public List<String> listVirtualDevices() {
    return myVirtualDevices.getValue();
  }

  @NotNull
  protected abstract List<String> listDevicesNonCached();

  @NotNull
  protected abstract List<String> listVirtualDevicesNonCached();

  @NotNull
  public abstract String platform();
}
