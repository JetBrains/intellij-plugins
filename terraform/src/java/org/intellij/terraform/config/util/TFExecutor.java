/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.terraform.config.util;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionHelper;
import com.intellij.execution.ExecutionModes;
import com.intellij.execution.RunContentExecutor;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.configurations.PtyCommandLine;
import com.intellij.execution.process.*;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Consumer;
import com.intellij.util.EmptyConsumer;
import com.intellij.util.ObjectUtils;
import org.intellij.terraform.config.TerraformConstants;
import org.intellij.terraform.hcl.HCLBundle;
import org.intellij.terraform.runtime.TerraformToolProjectSettings;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.*;

@SuppressWarnings("unused")
public final class TFExecutor {
  private static final Logger LOGGER = Logger.getInstance(TFExecutor.class);
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

  private TFExecutor(@NotNull Project project) {
    myProject = project;
    myExePath = TerraformToolProjectSettings.getInstance(myProject).getTerraformPath();
  }

  public static TFExecutor in(@NotNull Project project, @Nullable Module module) {
    return module != null ? in(module) : in(project);
  }

  private static @NotNull TFExecutor in(@NotNull Project project) {
    return new TFExecutor(project);
  }

  public static @NotNull TFExecutor in(@NotNull Module module) {
    // TODO: Decide whether 'module' is really needed
    return new TFExecutor(module.getProject());
  }

  public @NotNull TFExecutor withPresentableName(@Nullable @Nls String presentableName) {
    myPresentableName = presentableName;
    return this;
  }

  public @NotNull TFExecutor withExePath(@Nullable String exePath) {
    myExePath = exePath;
    return this;
  }

  public @NotNull TFExecutor withWorkDirectory(@Nullable String workDirectory) {
    myWorkDirectory = workDirectory;
    return this;
  }

  public TFExecutor withProcessListener(@NotNull ProcessListener listener) {
    myProcessListeners.add(listener);
    return this;
  }

  public @NotNull TFExecutor withExtraEnvironment(@NotNull Map<String, String> environment) {
    myExtraEnvironment.putAll(environment);
    return this;
  }

  public @NotNull TFExecutor withPassParentEnvironment(boolean passParentEnvironment) {
    myParentEnvironmentType = passParentEnvironment ? GeneralCommandLine.ParentEnvironmentType.CONSOLE
        : GeneralCommandLine.ParentEnvironmentType.NONE;
    return this;
  }

  public @NotNull TFExecutor withParameterString(@NotNull String parameterString) {
    myParameterList.addParametersString(parameterString);
    return this;
  }

  public @NotNull TFExecutor withParameters(String @NotNull ... parameters) {
    myParameterList.addAll(parameters);
    return this;
  }

  public @NotNull TFExecutor withParameters(@NotNull List<String> parameters) {
    myParameterList.addAll(parameters);
    return this;
  }

  public @NotNull TFExecutor showOutputOnError() {
    myShowOutputOnError = true;
    return this;
  }

  public @NotNull TFExecutor disablePty() {
    myPtyDisabled = true;
    return this;
  }

  public @NotNull TFExecutor showNotifications(boolean onError, boolean onSuccess) {
    myShowNotificationsOnError = onError;
    myShowNotificationsOnSuccess = onSuccess;
    return this;
  }

  public boolean execute() {
    ApplicationManager.getApplication().assertIsNonDispatchThread();
    Logger.getInstance(getClass()).assertTrue(myProcessHandler == null, "Process has already run with this executor instance");
    final Ref<Boolean> result = Ref.create(false);
    GeneralCommandLine commandLine = null;
    try {
      commandLine = createCommandLine();
      GeneralCommandLine finalCommandLine = commandLine;
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
              showNotification(HCLBundle.message("TFExecutor.finished.successfully.notification.text"), NotificationType.INFORMATION);
            }
          } else if (cancelledByUser) {
            if (myShowNotificationsOnError) {
              showNotification(HCLBundle.message("TFExecutor.interrupted.notification.text"), NotificationType.WARNING);
            }
          } else if (myShowOutputOnError) {
            ApplicationManager.getApplication().invokeLater(() -> showOutput(myProcessHandler, historyProcessListener));
          }
        }
      };

      myProcessHandler.addProcessListener(processAdapter);
      myProcessHandler.startNotify();
      ExecutionModes.SameThreadMode sameThreadMode = new ExecutionModes.SameThreadMode(getPresentableName());
      ExecutionHelper.executeExternalProcess(myProject, myProcessHandler, sameThreadMode, commandLine);

      LOGGER.debug("Finished `" + getPresentableName() + "` with result: " + result.get());
      return result.get();
    } catch (ExecutionException e) {
      if (myShowOutputOnError) {
        ExecutionHelper.showErrors(myProject, Collections.singletonList(e), getPresentableName(), null);
      }
      if (myShowNotificationsOnError) {
        showNotification(StringUtil.notNullize(e.getMessage(), HCLBundle.message("TFExecutor.unknown.error.notification.text")), NotificationType.ERROR);
      }
      String commandLineInfo = commandLine != null ? commandLine.getCommandLineString() : "not constructed";
      LOGGER.debug("Finished `" + getPresentableName() + "` with an exception. Commandline: " + commandLineInfo, e);
      return false;
    }
  }

  public void executeWithProgress(boolean modal) {
    executeWithProgress(modal, EmptyConsumer.getInstance());
  }

  public void executeWithProgress(final boolean modal, final @NotNull Consumer<? super Boolean> consumer) {
    ProgressManager.getInstance().run(new Task.Backgroundable(myProject, getPresentableName(), true) {
      private boolean doNotStart;

      @Override
      public void onCancel() {
        doNotStart = true;
        ProcessHandler handler = getProcessHandler();
        if (handler != null) {
          handler.destroyProcess();
        }
      }

      @Override
      public boolean shouldStartInBackground() {
        return !modal;
      }

      @Override
      public boolean isConditionalModal() {
        return modal;
      }

      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        if (doNotStart || myProject == null || myProject.isDisposed()) {
          return;
        }
        indicator.setIndeterminate(true);
        consumer.consume(execute());
      }
    });
  }

  public @Nullable ProcessHandler getProcessHandler() {
    return myProcessHandler;
  }

  private void showNotification(final @NotNull @Nls String message, final NotificationType type) {
    ApplicationManager.getApplication().invokeLater(
        () -> {
          String title = getPresentableName();
          Notifications.Bus.notify(TerraformConstants.EXECUTION_NOTIFICATION_GROUP.createNotification(title, message, type), myProject);
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
      showNotification(HCLBundle.message("TFExecutor.failed.to.run.notification.text"), NotificationType.ERROR);
    }
  }

  public @NotNull GeneralCommandLine createCommandLine() throws ExecutionException {
    GeneralCommandLine commandLine = !myPtyDisabled && PtyCommandLine.isEnabled() ? new PtyCommandLine() : new GeneralCommandLine();
    commandLine.setExePath(Objects.requireNonNull(myExePath));
    commandLine.getEnvironment().putAll(myExtraEnvironment);

    commandLine.withWorkDirectory(myWorkDirectory);
    commandLine.addParameters(myParameterList.getList());
    commandLine.withParentEnvironmentType(myParentEnvironmentType);
    commandLine.withCharset(StandardCharsets.UTF_8);
    return commandLine;
  }

  private @NotNull @Nls String getPresentableName() {
    return ObjectUtils.notNull(myPresentableName, HCLBundle.message("terraform.name.lowercase"));
  }
}
