package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.apache.commons.codec.Charsets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PhoneGapCommandLine {
  public static final Logger LOGGER = Logger.getInstance(PhoneGapCommandLine.class);

  public static final String INFO_PHONEGAP = "the following plugins are installed";

  @Nullable
  private final String myWorkDir;

  @NotNull
  private final String myPath;

  @Nullable
  private String version;
  private boolean myIsCorrect = true;

  public static final Function<String, String> REMOVE_QUOTE_AND_TRIM = new Function<String, String>() {
    @Override
    public String fun(String s) {
      return s.replace("'", "").trim();
    }
  };

  public PhoneGapCommandLine(@NotNull String path, @Nullable String dir) {
    myWorkDir = dir;
    myPath = path;
    try {
      version = executeAndReturnResult(myPath, "--version").replace("\"", "").trim();
    }
    catch (Exception e) {
      version = null;
      LOGGER.debug(e.getMessage(), e);
      myIsCorrect = false;
    }
  }

  public boolean isCorrectExecutable() {
    return myIsCorrect;
  }

  public String version() {
    return version;
  }

  public void pluginAdd(String fqn) {
    executeVoidCommand(myPath, "plugin", "add", fqn);
  }

  public void pluginRemove(String fqn) {
    executeVoidCommand(myPath, "plugin", "remove", fqn);
  }

  public ProcessOutput pluginListRaw() throws ExecutionException {
    return executeAndGetOut(new String[]{myPath, "plugin", "list"});
  }

  public boolean isOld() {
    if (StringUtil.isEmpty(version)) return false;

    String[] split = version.split("\\.");
    int first = Integer.parseInt(split[0]);
    if (first > 3) return false;
    if (first == 3) {
      return (split.length < 2 || Integer.parseInt(split[1]) < 5);
    }

    return first < 3;
  }

  public List<String> pluginList() {
    try {
      String out = executeAndReturnResult(myPath, "plugin", "list").trim();
      return parsePluginList(out);
    }
    catch (RuntimeException e) {
      LOGGER.debug(e.getMessage(), e);
      return Collections.emptyList();
    }
  }

  static List<String> parsePluginList(String out) {
    if (StringUtil.isEmpty(out) || StringUtil.contains(out.toLowerCase(Locale.getDefault()), "no plugins")) {
      return ContainerUtil.newArrayList();
    }

    if (out.startsWith("[") && out.endsWith("]")) {
      out = out.substring(1, out.length() - 1);
      return ContainerUtil.map(out.split(","), REMOVE_QUOTE_AND_TRIM);
    }

    if (out.startsWith("[")) {
      out = out.replaceAll("\\[(.*?)\\]", "");
    }
    List<String> plugins = ContainerUtil.map(out.split("\n"), StringUtil.TRIMMER);
    String item = ContainerUtil.getFirstItem(plugins);
    if (item != null && item.contains(INFO_PHONEGAP)) {
      plugins = plugins.subList(1, plugins.size());
    }
    return plugins;
  }


  private void executeVoidCommand(final String... command) {
    try {
      ProcessOutput output = executeAndGetOut(command);

      if (output.getExitCode() > 0) {
        throw new RuntimeException("Command error: " + output.getStderr());
      }
    }
    catch (Exception e) {
      LOGGER.debug(e.getMessage(), e);

      throw new RuntimeException("Select correct executable path", e);
    }
  }

  private String executeAndReturnResult(String... command) {
    try {
      final ProcessOutput output = executeAndGetOut(command);

      if (output.getExitCode() > 0) {
        throw new RuntimeException("Command error: " + output.getStderr());
      }

      return output.getStdout();
    }
    catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  private ProcessOutput executeAndGetOut(String[] command) throws ExecutionException {
    final GeneralCommandLine commandLine = new GeneralCommandLine(command);
    commandLine.setWorkDirectory(myWorkDir);
    Process process = commandLine.createProcess();
    OSProcessHandler processHandler = new ColoredProcessHandler(process, commandLine.getCommandLineString(), Charsets.UTF_8);
    final ProcessOutput output = new ProcessOutput();
    processHandler.addProcessListener(new ProcessAdapter() {
      @Override
      public void onTextAvailable(ProcessEvent event, Key outputType) {
        if (outputType == ProcessOutputTypes.STDERR) {
          output.appendStderr(event.getText());
        }
        else if (outputType != ProcessOutputTypes.SYSTEM) {
          output.appendStdout(event.getText());
        }
      }
    });
    processHandler.startNotify();
    if (processHandler.waitFor(TimeUnit.SECONDS.toMillis(60))) {
      output.setExitCode(process.exitValue());
    }
    else {
      processHandler.destroyProcess();
      output.setTimeout();
    }
    return output;
  }
}
