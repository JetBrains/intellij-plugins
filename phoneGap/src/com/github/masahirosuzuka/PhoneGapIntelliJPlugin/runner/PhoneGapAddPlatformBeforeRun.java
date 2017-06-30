package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.PhoneGapBundle;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapCommandLine;
import com.intellij.execution.BeforeRunTask;
import com.intellij.execution.BeforeRunTaskProvider;
import com.intellij.execution.ExecutionHelper;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Ref;
import com.intellij.util.concurrency.Semaphore;
import com.intellij.util.containers.ContainerUtil;
import icons.PhoneGapIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PhoneGapAddPlatformBeforeRun extends BeforeRunTaskProvider<PhoneGapAddPlatformBeforeRun.PhoneGapAddPlatformTask> {

  public static final Key<PhoneGapAddPlatformTask> ID = Key.create("PhonegapTask");

  @Override
  public Key<PhoneGapAddPlatformTask> getId() {
    return ID;
  }

  @Override
  public String getName() {
    return PhoneGapBundle.message("phonegap.before.task.init.default.title");
  }

  @Override
  public String getDescription(PhoneGapAddPlatformTask task) {
    return PhoneGapBundle.message("phonegap.before.task.init.default.title");
  }

  @Override
  public boolean isConfigurable() {
    return false;
  }


  @Override
  public Icon getIcon() {
    return PhoneGapIcons.PhonegapIntegration;
  }

  @Nullable
  @Override
  public PhoneGapAddPlatformTask createTask(@NotNull RunConfiguration runConfiguration) {
    return runConfiguration instanceof PhoneGapRunConfiguration ? new PhoneGapAddPlatformTask(ID) : null;
  }

  @Override
  public boolean configureTask(RunConfiguration runConfiguration, PhoneGapAddPlatformTask task) {
    return false;
  }

  @Override
  public boolean canExecuteTask(RunConfiguration configuration, PhoneGapAddPlatformTask task) {
    return configuration instanceof PhoneGapRunConfiguration;
  }

  @Override
  public boolean executeTask(DataContext context,
                             final RunConfiguration configuration,
                             ExecutionEnvironment env,
                             PhoneGapAddPlatformTask task) {

    final PhoneGapRunConfiguration phoneGapRunConfiguration = (PhoneGapRunConfiguration)configuration;
    final PhoneGapCommandLine line = phoneGapRunConfiguration.getCommandLine();

    if (!line.needAddPlatform()) {
      return true;
    }

    final Project project = configuration.getProject();
    final Semaphore targetDone = new Semaphore();
    final Ref<Boolean> result = new Ref<>(true);
    final List<Exception> exceptions = new ArrayList<>();
    ApplicationManager.getApplication().invokeAndWait(() -> {

      //Save all opened documents
      FileDocumentManager.getInstance().saveAllDocuments();
      targetDone.down();

      new Task.Backgroundable(project, PhoneGapBundle.message("phonegap.before.task.init.title", line.getPlatformName()), true) {

        public boolean shouldStartInBackground() {
          return true;
        }

        public void run(@NotNull final ProgressIndicator indicator) {
          try {
            String platform = phoneGapRunConfiguration.getPlatform();
            assert platform != null;
            ProcessOutput output = line.platformAdd(platform);
            if (output.getExitCode() != 0) {
              ExecutionHelper
                .showOutput(project, output, PhoneGapBundle.message("phonegap.before.task.init.title", line.getPlatformName()), null, true);
              result.set(false);
            }
          }
          catch (final Exception e) {
            exceptions.add(e);
            result.set(false);
          }
          finally {
            targetDone.up();
          }
        }
      }.queue();
    }, ModalityState.NON_MODAL);

    if (!targetDone.waitFor(TimeUnit.MINUTES.toMillis(2))) {
      ExecutionHelper
        .showErrors(project, ContainerUtil.createMaybeSingletonList(new RuntimeException("Timeout")),
                    PhoneGapBundle.message("phonegap.before.task.init.error"), null);
    }
    else if (!exceptions.isEmpty()) {
      ExecutionHelper.showErrors(project, exceptions, PhoneGapBundle.message("phonegap.before.task.init.error"), null);
    }


    return result.get();
  }

  public static class PhoneGapAddPlatformTask extends BeforeRunTask<PhoneGapAddPlatformTask> {

    protected PhoneGapAddPlatformTask(@NotNull Key<PhoneGapAddPlatformTask> providerId) {
      super(providerId);
      setEnabled(true);
    }
  }
}
