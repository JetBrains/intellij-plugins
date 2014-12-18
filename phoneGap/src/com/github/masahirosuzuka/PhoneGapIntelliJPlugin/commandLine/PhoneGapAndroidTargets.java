package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine;


import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner.ui.PhoneGapRunConfigurationEditor;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.PathEnvironmentVariableUtil;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.execution.util.ExecUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ExceptionUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner.ui.PhoneGapRunConfigurationEditor.PLATFORM_ANDROID;

public class PhoneGapAndroidTargets extends PhoneGapTargets {

  private static Logger LOGGER = Logger.getInstance(PhoneGapAndroidTargets.class);

  public PhoneGapAndroidTargets(@NotNull Project project) {
    super(project);
  }

  @NotNull
  public static String getAndroidName() {
    return SystemInfo.isWindows ? "android" + ".bat" : "android";
  }

  private static String getAdbName() {
    return SystemInfo.isWindows ? "adb" + ".exe" : "adb";
  }


  private List<String> getAndroidDevices(List<String> commands) throws ExecutionException {
    List<String> result = ContainerUtil.newArrayList();
    ProcessOutput output =
      ExecUtil.execAndGetOutput(commands, myProject.getBasePath());

    if (output.getExitCode() == 0 && StringUtil.isEmpty(output.getStderr())) {
      String[] split = StringUtil.splitByLines(output.getStdout());
      for (String s : split) {
        String device = s.trim();
        if (!StringUtil.isEmpty(device) && !isAndroidExcludedStrings(device)) {
          result.add(device);
        }
      }
    }

    return result;
  }

  private static boolean isAndroidExcludedStrings(String device) {
    //possible output:
    //* daemon not running. starting it now on port 5037 *
    //* daemon started successfully *
    //List of devices attached
    return device.startsWith("List of devices attached") ||
           (device.startsWith("* ") && device.endsWith(" *"));
  }

  private static List<String> splitNames(List<String> adbDevices) {
    return ContainerUtil.map(adbDevices, new Function<String, String>() {
      @Override
      public String fun(String s) {
        return s.split("\t")[0];
      }
    });
  }

  @NotNull
  @Override
  protected List<String> listDevicesNonCached() {
    List<String> androidDevices = ContainerUtil.newArrayList();
    try {
      File pathAdb = PathEnvironmentVariableUtil.findInPath(getAdbName());
      if (pathAdb != null && pathAdb.exists()) {
        androidDevices.addAll(splitNames(getAndroidDevices(ContainerUtil.newArrayList(pathAdb.getPath(), "devices"))));
      }
    }
    catch (ExecutionException e) {
      LOGGER.debug(e.getMessage(), e);
    }

    return androidDevices;
  }

  @NotNull
  @Override
  protected List<String> listVirtualDevicesNonCached() {
    List<String> androidVirtualDevices = ContainerUtil.newArrayList();
    try {
      File path = PathEnvironmentVariableUtil.findInPath(getAndroidName());
      if (path != null && path.exists()) {
        androidVirtualDevices.addAll(getAndroidDevices(ContainerUtil.newArrayList(path.getPath(), "-v", "list", "avd", "-c")));
      }
    }
    catch (ExecutionException e) {
      LOGGER.debug(e.getMessage(), e);
    }
    return androidVirtualDevices;
  }

  @NotNull
  @Override
  public String platform() {
    return PLATFORM_ANDROID;
  }
}
