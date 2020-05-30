package com.jetbrains.cidr.cpp.embedded.platformio.ui;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioService;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public abstract class PlatformioAction extends PlatformioActionBase {
  public PlatformioAction(@NotNull Supplier<String> dynamicText, @NotNull Supplier<String> dynamicDescription) {
    super(dynamicText, dynamicDescription);
  }

  public PlatformioAction(@NotNull Supplier<String> dynamicText) {
    this(dynamicText, () -> null);
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    super.update(e);

    Presentation presentation = e.getPresentation();
    if (presentation.isVisible()) {
      presentation.setEnabled(PlatformioService.getState(e.getProject()) != PlatformioService.State.NONE);
    }
  }
}
