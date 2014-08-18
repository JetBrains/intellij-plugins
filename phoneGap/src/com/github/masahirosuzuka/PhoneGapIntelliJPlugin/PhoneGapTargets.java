package com.github.masahirosuzuka.PhoneGapIntelliJPlugin;


import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ui.PhoneGapRunConfigurationEditor;
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
  private List<String> androidTargets = null;

  public PhoneGapTargets(@NotNull Project project) {
    myProject = project;
  }


  public List<String> getTargets(String platform) {
    if (PhoneGapRunConfigurationEditor.PLATFORM_ANDROID.equals(platform)) {
      return getAndroidTargets();
    }

    return ContainerUtil.emptyList();
  }

  private synchronized List<String> getAndroidTargets() {
    if (androidTargets == null) {
      androidTargets = ContainerUtil.newArrayList();
      try {
        File path = PathEnvironmentVariableUtil.findInPath(SystemInfo.isWindows ? "android" + ".exe" : "android");

        if (path != null && path.exists()) {
          androidTargets.addAll(addAndroidDevices(ContainerUtil.newArrayList(path.getPath(), "-v", "list", "avd", "-c")));
        }

        File pathAdb = PathEnvironmentVariableUtil.findInPath(SystemInfo.isWindows ? "adb" + ".exe" : "adb");
        if (pathAdb != null && pathAdb.exists()) {
          androidTargets.addAll(splitNames(addAndroidDevices(ContainerUtil.newArrayList(pathAdb.getPath(), "devices"))));
        }
      }
      catch (ExecutionException e) {
        LOGGER.debug(e.getMessage(), e);
      }
    }
    return androidTargets;
  }


  private List<String> addAndroidDevices(List<String> commands) throws ExecutionException {
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
