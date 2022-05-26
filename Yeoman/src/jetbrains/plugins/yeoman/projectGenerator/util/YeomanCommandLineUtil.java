package jetbrains.plugins.yeoman.projectGenerator.util;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.*;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.npm.NpmUtil;
import com.intellij.lang.javascript.bower.BowerCommandLineUtil;
import com.intellij.lang.javascript.bower.BowerSettings;
import com.intellij.lang.javascript.bower.BowerSettingsManager;
import com.intellij.lang.javascript.buildTools.npm.rc.NpmCommand;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Ref;
import com.intellij.util.Alarm;
import jetbrains.plugins.yeoman.projectGenerator.template.YeomanProjectGenerator;
import jetbrains.plugins.yeoman.settings.YeomanGlobalSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.concurrent.TimeUnit;


public final class YeomanCommandLineUtil {

  public static Runnable createExecuteCommandLineAction(
    @Nullable final Project project,
    @NotNull final GeneralCommandLine commandLine,
    @NotNull final Ref<? super RuntimeException> exceptionRef,
    @Nullable final ProgressIndicator currentIndicator) {

    return new Runnable() {
      @Override
      public void run() {
        final ProgressIndicator indicator =
          currentIndicator == null ? ProgressManager.getInstance().getProgressIndicator() : currentIndicator;

        indicator.setText(commandLine.getCommandLineString());
        final ProcessOutput output = new ProcessOutput();
        try {
          ProcessListener listener = new ProcessAdapter() {
            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
              if (event == null) {
                return;
              }
              String text = event.getText().trim();
              if (outputType == ProcessOutputTypes.STDERR || outputType == ProcessOutputTypes.STDOUT) {
                indicator.setText2(text);
              }
            }
          };
          final OSProcessHandler processHandler = new KillableColoredProcessHandler(commandLine);


          final Alarm alarm = project == null ? null : new Alarm(Alarm.ThreadToUse.POOLED_THREAD, project);
          if (alarm != null) {
            alarm.addRequest(new Runnable() {
              @Override
              public void run() {
                if (!processHandler.isProcessTerminated()) {
                  if (indicator.isCanceled()) {
                    processHandler.destroyProcess();
                  }
                  else {
                    alarm.addRequest(this, TimeUnit.SECONDS.toMillis(1));
                  }
                }
              }
            }, TimeUnit.SECONDS.toMillis(1));
          }

          processHandler.addProcessListener(new ProcessAdapter() {
            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
              if (outputType == ProcessOutputTypes.STDERR) {
                output.appendStderr(event.getText());
              }
              else if (outputType != ProcessOutputTypes.SYSTEM) {
                output.appendStdout(event.getText());
              }
            }

            @Override
            public void processTerminated(@NotNull ProcessEvent event) {
              if (alarm != null) {
                Disposer.dispose(alarm);
              }
            }
          });
          processHandler.addProcessListener(listener);
          processHandler.startNotify();
          if (processHandler.waitFor((int)TimeUnit.MINUTES.toMillis(10))) {
            try {
              output.setExitCode(processHandler.getProcess().exitValue());
            }
            catch (Exception e) {
              throw new ExecutionException(e);
            }
          }
          else {
            processHandler.destroyProcess();
            output.setTimeout();
          }
        }
        catch (ExecutionException e) {
          exceptionRef.set(new RuntimeException("Cannot execute " + commandLine.getCommandLineString(), e));
          return;
        }
        String errorMessage = null;
        if (output.isTimeout()) {
          errorMessage = "Time limit exceeded for command:\n" + commandLine.getCommandLineString();
        }
        if (output.getExitCode() != 0) {
          errorMessage = "Failed command:\n" + commandLine.getCommandLineString()
                         + "\nExit code: " + output.getExitCode();
        }
        if (errorMessage != null) {
          if (!output.getStdout().isEmpty()) {
            errorMessage += "\nStandard output:\n" + output.getStdout();
          }
          if (!output.getStderr().isEmpty()) {
            errorMessage += "\nStandard error:\n" + output.getStderr();
          }
          exceptionRef.set(new RuntimeException(errorMessage));
        }
      }
    };
  }

  public static GeneralCommandLine createBowerInstallCommandLine(@NotNull Project project, @NotNull File workingDirectory) {
    final BowerSettings settings = BowerSettingsManager.getInstance(project).getSettings();
    final BowerSettings build = settings.createBuilder()
      .setBowerJsonPath(new File(workingDirectory, YeomanProjectGenerator.BOWER_JSON).getAbsolutePath())
      .build();

    try {
      GeneralCommandLine line = BowerCommandLineUtil.createCommandLine(build);
      line.addParameter("install");
      return line;
    }
    catch (ExecutionException e) {
      return null;
    }
  }

  @NotNull
  public static GeneralCommandLine createNpmInstallCommandLine(@NotNull Project project, @NotNull File workingDirectory) {
    final YeomanGlobalSettings files = YeomanGlobalSettings.getInstance();

    NodeJsInterpreter interpreter = files.getInterpreter();

    assert interpreter != null;

    try {
      return NpmUtil.createNpmCommandLine(project, workingDirectory, interpreter, NpmCommand.INSTALL, Collections.emptyList());
    }
    catch (ExecutionException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
