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
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class PhoneGapTargets {
  private static Logger LOGGER = Logger.getInstance(PhoneGapTargets.class);

  private final Project myProject;
  private List<String> androidVirtualDevices = null;
  private List<String> androidDevices = null;

  public PhoneGapTargets(@NotNull Project project) {
    myProject = project;
  }


  public List<String> getTargets(String platform, String commands) {
    try {
      if (PhoneGapRunConfigurationEditor.PLATFORM_ANDROID.equals(platform)) {
        if (PhoneGapCommandLine.COMMAND_RUN.equals(commands)) {
          return getAndroidDevices();
        }

        if (PhoneGapCommandLine.COMMAND_EMULATE.equals(commands)) {
          return getAndroidVirtualDevices();
        }
      }
    }
    catch (ExecutionException e) {
      LOGGER.debug(e.getMessage(), e);
    }

    return ContainerUtil.emptyList();
  }

  private synchronized List<String> getAndroidVirtualDevices() throws ExecutionException {
    if (androidVirtualDevices == null) {
      androidVirtualDevices = ContainerUtil.newArrayList();
      File path = PathEnvironmentVariableUtil.findInPath(getAndroidName());
      if (path != null && path.exists()) {
        androidVirtualDevices.addAll(getAndroidDevices(ContainerUtil.newArrayList(path.getPath(), "-v", "list", "avd", "-c")));
      }
    }
    return androidVirtualDevices;
  }

  @NotNull
  public static String getAndroidName() {
    return SystemInfo.isWindows ? "android" + ".bat" : "android";
  }

  public static String getIosSimName() {
    return "ios-sim";
  }

  private synchronized List<String> getAndroidDevices() throws ExecutionException {
    if (androidDevices == null) {
      androidDevices = ContainerUtil.newArrayList();
      File pathAdb = PathEnvironmentVariableUtil.findInPath(getAdbName());
      if (pathAdb != null && pathAdb.exists()) {
        androidDevices.addAll(splitNames(getAndroidDevices(ContainerUtil.newArrayList(pathAdb.getPath(), "devices"))));
      }
    }
    return androidDevices;
  }

  private static String getAdbName() {
    return SystemInfo.isWindows ? "adb" + ".exe" : "adb";
  }


  private List<String> getAndroidDevices(List<String> commands) throws ExecutionException {
    List<String> result = ContainerUtil.newArrayList();
    ProcessOutput output =
      ExecUtil.execAndGetOutput(commands, myProject.getBasePath());

    if (output.getExitCode() == 0 && StringUtil.isEmpty(output.getStderr())) {
      String[] split = output.getStdout().split("\n");
      for (String s : split) {
        String device = s.trim();
        if (!StringUtil.isEmpty(device) && !device.startsWith("List of devices attached")) {
          result.add(device);
        }
      }
    }

    return result;
  }

  private static List<String> splitNames(List<String> adbDevices) {
    return ContainerUtil.map(adbDevices, new Function<String, String>() {
      @Override
      public String fun(String s) {
        return s.split("\t")[0];
      }
    });
  }
}
