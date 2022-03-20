package com.jetbrains.cidr.cpp.embedded.platformio;

import com.intellij.execution.BeforeRunTask;
import com.intellij.execution.BeforeRunTaskProvider;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Ref;
import com.intellij.util.concurrency.Semaphore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static com.intellij.openapi.application.ApplicationManager.getApplication;
import static com.intellij.openapi.application.ModalityState.NON_MODAL;
import static com.intellij.openapi.ui.MessageType.ERROR;
import static com.intellij.util.ModalityUiUtil.invokeLaterIfNeeded;
import static com.jetbrains.cidr.cpp.embedded.platformio.ui.PlatformioCleanAction.createPlatformioTool;
import static com.jetbrains.cidr.execution.build.CidrBuild.showBuildNotification;
import static icons.ClionEmbeddedPlatformioIcons.Platformio;


public class PlatformioCleanBeforeRunTaskProvider extends BeforeRunTaskProvider<BeforeRunTask<?>> {
  public static final Key<BeforeRunTask<?>> ID = Key.create("PlatformioPluginTarget");
  private static final long executionId = ExecutionEnvironment.getNextUnusedExecutionId();

  @Override
  public @Nullable Icon getIcon() {
    return Platformio;
  }

  @Override
  public Key<BeforeRunTask<?>> getId() {
    return ID;
  }

  @Override
  public String getName() {
    return ClionEmbeddedPlatformioBundle.message("platformio.prebuild.clean");
  }

  @Nullable
  @Override
  public BeforeRunTask<?> createTask(final @NotNull RunConfiguration runConfiguration) {
    return new BeforeRunTask<>(ID) {
    };
  }

  @Override
  public boolean executeTask(final @NotNull DataContext context,
                             final @NotNull RunConfiguration configuration,
                             final @NotNull ExecutionEnvironment env,
                             final @NotNull BeforeRunTask<?> task) {
    final var tool = createPlatformioTool(configuration.getProject());
    final var success = new Ref<>(false);
    final var actionFinished = new Semaphore();
    if (tool != null) {
      final ProcessAdapter successListener = new ProcessAdapter() {
        @Override
        public void processTerminated(@NotNull ProcessEvent event) {
          success.set(event.getExitCode() == 0);
          actionFinished.up();
        }
      };
      actionFinished.down();
      getApplication().invokeLater(() ->
                                                    {
                                                        if (!tool.executeIfPossible(null, context, executionId, successListener)) {
                                                          actionFinished.up();
                                                        }
                                                      }
      );
      actionFinished.waitFor();
    }

    if (!success.get()) {
      invokeLaterIfNeeded(NON_MODAL, () -> showBuildNotification(configuration.getProject(), ERROR, ClionEmbeddedPlatformioBundle.message("platformio.clean.failed")));
    }
    return success.get();
  }
}
