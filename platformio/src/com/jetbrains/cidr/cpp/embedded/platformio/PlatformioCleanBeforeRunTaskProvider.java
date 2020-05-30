package com.jetbrains.cidr.cpp.embedded.platformio;

import com.intellij.execution.BeforeRunTask;
import com.intellij.execution.BeforeRunTaskProvider;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Ref;
import com.intellij.tools.Tool;
import com.intellij.ui.GuiUtils;
import com.intellij.util.concurrency.Semaphore;
import com.jetbrains.cidr.cpp.embedded.platformio.ui.PlatformioCleanAction;
import com.jetbrains.cidr.execution.build.CidrBuild;
import icons.ClionEmbeddedPlatformioIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;


public class PlatformioCleanBeforeRunTaskProvider extends BeforeRunTaskProvider<BeforeRunTask<?>> {
  public static final Key<BeforeRunTask<?>> ID = Key.create("PlatformioPluginTarget");
  private static final long executionId = ExecutionEnvironment.getNextUnusedExecutionId();

  @Override
  public @Nullable Icon getIcon() {
    return ClionEmbeddedPlatformioIcons.Platformio;
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
  public BeforeRunTask<?> createTask(@NotNull RunConfiguration runConfiguration) {
    return new BeforeRunTask<BeforeRunTask<?>>(ID) {
    };
  }

  @Override
  public boolean executeTask(@NotNull DataContext context,
                             @NotNull RunConfiguration configuration,
                             @NotNull ExecutionEnvironment env,
                             @NotNull BeforeRunTask<?> task) {
    Tool tool = PlatformioCleanAction.createPlatformioTool(configuration.getProject());
    Ref<Boolean> success = new Ref<>(false);
    Semaphore actionFinished = new Semaphore();
    if (tool != null) {

      ProcessAdapter successListener = new ProcessAdapter() {
        @Override
        public void processTerminated(@NotNull ProcessEvent event) {
          success.set(event.getExitCode() == 0);
          actionFinished.up();
        }
      };
      actionFinished.down();
      ApplicationManager.getApplication().invokeLater(() ->
                                                      {
                                                        if (!tool.executeIfPossible(null, context, executionId, successListener)) {
                                                          actionFinished.up();
                                                        }
                                                      }
      );
      actionFinished.waitFor();
    }

    if (!success.get()) {
      GuiUtils.invokeLaterIfNeeded(
        () -> CidrBuild.showBuildNotification(configuration.getProject(), MessageType.ERROR,
                                              ClionEmbeddedPlatformioBundle.message("platformio.clean.failed")),
        ModalityState.NON_MODAL);
    }
    return success.get();
  }
}
