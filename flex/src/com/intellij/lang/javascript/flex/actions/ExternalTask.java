// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.actions;

import com.intellij.flex.FlexCommonUtils;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.actions.airpackage.AdtPackageTask;
import com.intellij.lang.javascript.flex.actions.airpackage.AirPackageProjectParameters;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Consumer;
import com.intellij.util.TimeoutUtil;
import com.intellij.util.text.StringTokenizer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class ExternalTask {

  private static final Logger LOG = Logger.getInstance(ExternalTask.class.getName());

  protected final Project myProject;
  protected final Sdk myFlexSdk;

  private Process myProcess;
  private boolean myFinished;
  private String myCommandLine = "";
  protected List<String> myMessages = new ArrayList<>();
  private int myExitCode = -1;

  public ExternalTask(final Project project, final Sdk flexSdk) {
    myProject = project;
    myFlexSdk = flexSdk;
  }

  public void start() {
    final List<String> command = createCommandLine();

    for (String s : command) {
      if (s == null) {
        LOG.error(StringUtil.join(command, s1 -> s1 == null ? "null" : s1, " "));
      }
    }

    final ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.redirectErrorStream(true);

    processBuilder.directory(getProcessDir());
    prepareEnvVars(processBuilder.environment());

    myCommandLine = StringUtil.join(command, " ");
    debug("Executing task: " + myCommandLine);

    try {
      myProcess = processBuilder.start();
      scheduleInputStreamReading();
    }
    catch (IOException e) {
      myFinished = true;
      myMessages.add(e.getMessage());
    }
  }

  protected void prepareEnvVars(Map<String, String> envVars) {
  }

  @Nullable
  protected File getProcessDir() {
    return null;
  }

  private void debug(final String message) {
    LOG.debug("[" + hashCode() + "] " + message);
  }

  protected abstract List<String> createCommandLine();

  protected boolean checkMessages() {
    return myMessages.isEmpty();
  }

  public boolean isFinished() {
    return myFinished;
  }

  public void cancel() {
    if (myProcess != null) {
      myProcess.destroy();
      try {
        myExitCode = myProcess.exitValue();
        debug("Process complete with exit code " + myExitCode);
      }
      catch (IllegalThreadStateException e) {/*ignore*/}
    }

    myFinished = true;
  }

  public String getCommandLine() {
    return myCommandLine;
  }

  public List<String> getMessages() {
    return myMessages;
  }

  public int getExitCode() {
    return myExitCode;
  }

  protected Process getProcess() {
    return myProcess;
  }

  protected void scheduleInputStreamReading() {
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      boolean usageStarted = false;

      try (InputStreamReader reader = FlexCommonUtils.createInputStreamReader(myProcess.getInputStream())) {
        char[] buf = new char[4096];
        int read;
        while ((read = reader.read(buf, 0, buf.length)) >= 0) {
          final String output = new String(buf, 0, read);
          debug("Process output: " + output);
          if (!usageStarted) {
            final StringTokenizer tokenizer = new StringTokenizer(output, "\r\n");

            while (tokenizer.hasMoreElements()) {
              final String message = tokenizer.nextElement();
              if (!StringUtil.isEmptyOrSpaces(message)) {
                if (StringUtil.toLowerCase(message.trim()).startsWith("usage:")) {
                  usageStarted = true;
                  break;
                }

                if (message.trim().endsWith("password:")) {
                  final OutputStream outputStream = myProcess.getOutputStream();
                  outputStream.write("\n".getBytes(StandardCharsets.UTF_8));
                  outputStream.flush();
                }
                else {
                  myMessages.add(message);
                }
              }
            }
          }
        }
      }
      catch (IOException e) {
        myMessages.add(e.getMessage());
      }
      finally {
        cancel();
      }
    });
  }

  public static boolean runWithProgress(final ExternalTask task, final String progressTitle, final String frameTitle) {
    return ProgressManager.getInstance().runProcessWithProgressSynchronously(createRunnable(task), progressTitle, true, task.myProject)
           && checkMessages(task, frameTitle);
  }

  public static void runInBackground(final ExternalTask task,
                                     final String progressTitle,
                                     final @Nullable Consumer<? super List<String>> onSuccess,
                                     final @Nullable Consumer<? super List<String>> onFailure) {
    ProgressManager.getInstance().run(new Task.Backgroundable(task.myProject, progressTitle, true) {
      @Override
      public void run(@NotNull final ProgressIndicator indicator) {
        createRunnable(task).run();
      }

      @Override
      public void onSuccess() {
        if (task.checkMessages()) {
          if (onSuccess != null) {
            ApplicationManager.getApplication().invokeLater(() -> onSuccess.consume(task.getMessages()));
          }
        }
        else if (onFailure != null) {
          ApplicationManager.getApplication().invokeLater(() -> onFailure.consume(task.getMessages()));
        }
      }
    });
  }

  private static boolean checkMessages(final ExternalTask task, final String frameTitle) {
    final List<String> messages = task.getMessages();
    if (task.checkMessages()) {
      return true;
    }
    else {
      String message = messages.isEmpty() ? FlexBundle.message("unexpected.empty.adt.output") : StringUtil.join(messages, "\n");
      if (message.length() > 10000) {
        message = message.substring(0, 10000) + "...";
      }
      if (task instanceof AdtPackageTask) {
        message += "\n\nADT command line:\n" + task.getCommandLine();
      }
      Messages
        .showIdeaMessageDialog(task.myProject, message, frameTitle, new String[]{Messages.getOkButton()}, 0, Messages.getErrorIcon(), null);
    }
    return false;
  }

  private static Runnable createRunnable(final ExternalTask task) {
    return () -> {
      final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
      if (indicator != null) {
        indicator.setIndeterminate(true);
      }

      try {
        AirPackageProjectParameters.getInstance(task.myProject).setPackagingInProgress(true);

        task.start();

        while (!task.isFinished()) {
          if (indicator != null && indicator.isCanceled()) {
            task.cancel();
            break;
          }
          TimeoutUtil.sleep(200);
        }
      }
      finally {
        AirPackageProjectParameters.getInstance(task.myProject).setPackagingInProgress(false);
      }
    };
  }
}
