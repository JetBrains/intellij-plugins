// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.util;

import com.intellij.execution.*;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.configurations.PtyCommandLine;
import com.intellij.execution.process.*;
import com.intellij.execution.wsl.WSLCommandLineOptions;
import com.intellij.execution.wsl.WslPath;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread;
import org.intellij.terraform.config.TfConstants;
import org.intellij.terraform.hcl.HCLBundle;
import org.intellij.terraform.install.TfToolType;
import org.intellij.terraform.runtime.TfToolConfigurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

@SuppressWarnings("unused")
public final class TfExecutor {
  private static final Logger LOGGER = Logger.getInstance(TfExecutor.class);
  private final @NotNull Map<String, String> myExtraEnvironment = new HashMap<>();
  private final @NotNull ParametersList myParameterList = new ParametersList();
  private final @NotNull ProcessOutput myProcessOutput = new ProcessOutput();
  private final @NotNull Project myProject;
  private @Nullable String myWorkDirectory;
  private boolean myShowOutputOnError;
  private boolean myShowNotificationsOnError;
  private boolean myShowNotificationsOnSuccess;
  private GeneralCommandLine.ParentEnvironmentType myParentEnvironmentType = GeneralCommandLine.ParentEnvironmentType.CONSOLE;
  private boolean myPtyDisabled;
  private @Nullable String myExePath;
  private @Nullable @Nls String myPresentableName;
  private OSProcessHandler myProcessHandler;
  private final Collection<ProcessListener> myProcessListeners = new ArrayList<>();
  private final TfToolType myToolType;

  private TfExecutor(@NotNull Project project, TfToolType toolType) {
    myProject = project;
    myToolType = toolType;
    myExePath = toolType.getToolSettings(project).getToolPath();
  }

  public static @NotNull TfExecutor in(@NotNull Project project, TfToolType toolType) {
    return new TfExecutor(project, toolType);
  }

  public @NotNull TfExecutor withPresentableName(@Nullable @Nls String presentableName) {
    myPresentableName = presentableName;
    return this;
  }

  public @NotNull TfExecutor withExePath(@Nullable String exePath) {
    myExePath = exePath;
    return this;
  }

  public @NotNull TfExecutor withWorkDirectory(@Nullable String workDirectory) {
    myWorkDirectory = workDirectory;
    return this;
  }

  public TfExecutor withProcessListener(@NotNull ProcessListener listener) {
    myProcessListeners.add(listener);
    return this;
  }

  public @NotNull TfExecutor withExtraEnvironment(@NotNull Map<String, String> environment) {
    myExtraEnvironment.putAll(environment);
    return this;
  }

  public @NotNull TfExecutor withPassParentEnvironment(boolean passParentEnvironment) {
    myParentEnvironmentType = passParentEnvironment ? GeneralCommandLine.ParentEnvironmentType.CONSOLE
        : GeneralCommandLine.ParentEnvironmentType.NONE;
    return this;
  }

  public @NotNull TfExecutor withParameterString(@NotNull String parameterString) {
    myParameterList.addParametersString(parameterString);
    return this;
  }

  public @NotNull TfExecutor withParameters(String @NotNull ... parameters) {
    myParameterList.addAll(parameters);
    return this;
  }

  public @NotNull TfExecutor withParameters(@NotNull List<String> parameters) {
    myParameterList.addAll(parameters);
    return this;
  }

  public @NotNull TfExecutor showOutputOnError() {
    myShowOutputOnError = true;
    return this;
  }

  public @NotNull TfExecutor disablePty() {
    myPtyDisabled = true;
    return this;
  }

  public @NotNull TfExecutor showNotifications(boolean onError, boolean onSuccess) {
    myShowNotificationsOnError = onError;
    myShowNotificationsOnSuccess = onSuccess;
    return this;
  }

  public @Nullable String getWorkDirectory() {
    return myWorkDirectory;
  }

  public boolean execute() {
    return execute(new ExecutionModes.SameThreadMode(getPresentableName()));
  }

  @RequiresBackgroundThread
  public boolean execute(ExecutionMode executionMode) {
    Logger.getInstance(getClass()).assertTrue(myProcessHandler == null, "Process has already run with this executor instance");
    final Ref<Boolean> result = Ref.create(false);
    GeneralCommandLine commandLine = null;
    try {
      commandLine = createCommandLine();
      GeneralCommandLine finalCommandLine = ApplicationManager.getApplication()
        .getService(TfCommandLineService.class).wrapCommandLine(commandLine);

      myProcessHandler = new KillableColoredProcessHandler(finalCommandLine);
      final HistoryProcessListener historyProcessListener = new HistoryProcessListener();
      myProcessHandler.addProcessListener(historyProcessListener);
      for (ProcessListener listener : myProcessListeners) {
        myProcessHandler.addProcessListener(listener);
      }

      CapturingProcessAdapter processAdapter = new CapturingProcessAdapter(myProcessOutput) {
        @Override
        public void processTerminated(@NotNull ProcessEvent event) {
          super.processTerminated(event);
          boolean success = event.getExitCode() == 0 && myProcessOutput.getStderr().isEmpty();
          boolean nothingToShow = myProcessOutput.getStdout().isEmpty() && myProcessOutput.getStderr().isEmpty();
          boolean cancelledByUser = (event.getExitCode() == -1 || event.getExitCode() == 2) && nothingToShow;
          result.set(success);
          if (success) {
            if (myShowNotificationsOnSuccess) {
              showNotification(HCLBundle.message("TfExecutor.finished.successfully.notification.text"), NotificationType.INFORMATION);
            }
          } else if (cancelledByUser) {
            if (myShowNotificationsOnError) {
              showNotification(HCLBundle.message("TfExecutor.interrupted.notification.text"), NotificationType.WARNING);
            }
          } else if (myShowOutputOnError) {
            ApplicationManager.getApplication().invokeLater(() -> showOutput(myProcessHandler, historyProcessListener));
          }
        }
      };

      myProcessHandler.addProcessListener(processAdapter);
      myProcessHandler.startNotify();
      ExecutionHelper.executeExternalProcess(myProject, myProcessHandler, executionMode, commandLine);

      LOGGER.debug("Finished `" + getPresentableName() + "` with result: " + result.get());
      return result.get();
    } catch (ExecutionException e) {
      if (myShowOutputOnError) {
        ExecutionHelper.showErrors(myProject, Collections.singletonList(e), getPresentableName(), null);
      }
      if (myShowNotificationsOnError) {
        showNotification(StringUtil.notNullize(e.getMessage(), HCLBundle.message("TfExecutor.unknown.error.notification.text")), NotificationType.ERROR);
      }
      String commandLineInfo = commandLine != null ? commandLine.getCommandLineString() : "not constructed";
      LOGGER.debug("Finished `" + getPresentableName() + "` with an exception. Commandline: " + commandLineInfo, e);
      return false;
    }
  }

  public @Nullable ProcessHandler getProcessHandler() {
    return myProcessHandler;
  }

  private void showNotification(final @NotNull @Nls String message, final NotificationType type) {
    ApplicationManager.getApplication().invokeLater(
        () -> {
          String title = getPresentableName();
          Notification notification = TfConstants.getNotificationGroup().createNotification(title, message, type);
          if (type == NotificationType.ERROR) {
            notification.addAction(new NotificationAction(HCLBundle.message("terraform.open.settings")) {
              @Override
              public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                ShowSettingsUtil.getInstance().showSettingsDialog(e.getProject(), TfToolConfigurable.class);
              }
            });
          }
          Notifications.Bus.notify(notification, myProject);
        });
  }

  private void showOutput(@NotNull OSProcessHandler originalHandler, @NotNull HistoryProcessListener historyProcessListener) {
    if (myShowOutputOnError) {
      BaseOSProcessHandler outputHandler = new KillableColoredProcessHandler(originalHandler.getProcess(), null);
      RunContentExecutor runContentExecutor = new RunContentExecutor(myProject, outputHandler)
          .withTitle(getPresentableName())
          .withActivateToolWindow(myShowOutputOnError);
      Disposer.register(myProject, runContentExecutor);
      runContentExecutor.run();
      historyProcessListener.apply(outputHandler);
    }
    if (myShowNotificationsOnError) {
      showNotification(HCLBundle.message("TfExecutor.failed.to.run.notification.text"), NotificationType.ERROR);
    }
  }

  public @NotNull GeneralCommandLine createCommandLine() throws ExecutionException {
    String exePath =  Objects.requireNonNull(myExePath);
    WslPath wslPath = WslPath.parseWindowsUncPath(exePath);
    if(wslPath != null){
      exePath = wslPath.getLinuxPath();
    }
    GeneralCommandLine commandLine = !myPtyDisabled && PtyCommandLine.isEnabled() ? new PtyCommandLine() : new GeneralCommandLine();
    commandLine.withExePath(exePath);
    commandLine.getEnvironment().putAll(myExtraEnvironment);
    if (myWorkDirectory != null) {
      commandLine.withWorkingDirectory(Path.of(myWorkDirectory));
    }
    commandLine.addParameters(myParameterList.getList());
    commandLine.withParentEnvironmentType(myParentEnvironmentType);
    commandLine.withCharset(StandardCharsets.UTF_8);
    if(wslPath != null){
      var wslOptions = new WSLCommandLineOptions()
        .setExecuteCommandInShell(false)
        .setPassEnvVarsUsingInterop(true);
      commandLine = wslPath.getDistribution().patchCommandLine(commandLine, myProject, wslOptions);
    }
    return commandLine;
  }

  @NotNull @Nls String getPresentableName() {
    return ObjectUtils.notNull(myPresentableName, myToolType.getDisplayName());
  }
}
