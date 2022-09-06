package com.jetbrains.cidr.cpp.embedded.platformio.ui;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioService;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public abstract class PlatformioAction extends PlatformioActionBase {
  public PlatformioAction(@NotNull Supplier<String> dynamicText, @NotNull Supplier<String> dynamicDescription) {
    super(dynamicText, dynamicDescription);
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.EDT;
  }

  public PlatformioAction(@NotNull Supplier<String> dynamicText) {
    this(dynamicText, () -> null);
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    PlatformioService.State state = PlatformioService.getState(e.getProject());
    e.getPresentation().setEnabled(state != PlatformioService.State.NONE);
  }
}
