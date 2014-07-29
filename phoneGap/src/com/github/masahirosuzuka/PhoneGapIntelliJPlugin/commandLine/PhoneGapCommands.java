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

public class PhoneGapCommands {
  public static final Logger LOGGER = Logger.getInstance(PhoneGapCommands.class);

  @Nullable
  private final String myWorkDir;

  @NotNull
  private final String myPath;

  public static final Function<String, String> REMOVE_QUOTE_AND_TRIM = new Function<String, String>() {
    @Override
    public String fun(String s) {
      return s.replace("'", "").trim();
    }
  };

  public PhoneGapCommands(@NotNull String path, @Nullable String dir) {
    myWorkDir = dir;
    this.myPath = path;
  }

  public void version() {
    executeVoidCommand(myPath, "--version");
  }

  public void pluginAdd(String fqn) {
    executeVoidCommand(myPath, "plugin", "add", fqn);
  }

  public void pluginRemove(String fqn) {
    executeVoidCommand(myPath, "plugin", "remove", fqn);
  }

  public List<String> pluginList() {
    try {
      String out = executeAndReturnResult(myPath, "plugin", "list").trim();
      if (StringUtil.isEmpty(out)) {
        return ContainerUtil.newArrayList();
      }
      if (out.startsWith("[")) {
        if (out.endsWith("]")) {
          //cordova like out
          //[ 'name1', 'name2', 'name3' ]
          out = out.substring(1, out.length() - 1);
        }
        else {
          //it is phonegap like out
          //[phonegap] pluginName1
          //[phonegap] pluginName2 ...
          out = out.replaceAll("\\[(.*?)\\]", "");
        }
      }

      return out.contains("\n")
             ? ContainerUtil.map(out.split("\n"), StringUtil.TRIMMER)
             : ContainerUtil.map(out.split(","), REMOVE_QUOTE_AND_TRIM);
    }
    catch (RuntimeException e) {
      LOGGER.debug(e.getMessage(), e);
      return Collections.emptyList();
    }
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

      String text = output.getStdout();
      if (StringUtil.contains(text.toLowerCase(Locale.getDefault()), "no plugins")) {
        return "";
      }
      return text;
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
