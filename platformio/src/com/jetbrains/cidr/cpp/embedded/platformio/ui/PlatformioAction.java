package com.jetbrains.cidr.cpp.embedded.platformio.ui;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioService.State.NONE;
import static com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioService.getState;

public abstract class PlatformioAction extends PlatformioActionBase {
  public PlatformioAction(
          final @NotNull Supplier<String> dynamicText,
          final @NotNull Supplier<String> dynamicDescription) {
    super(dynamicText, dynamicDescription);
  }

  public PlatformioAction(final @NotNull Supplier<String> dynamicText) {
    this(dynamicText, () -> null);
  }

  @Override
  public void update(final @NotNull AnActionEvent e) {
    super.update(e);

    final var presentation = e.getPresentation();
    if (presentation.isVisible()) {
      presentation.setEnabled(getState(e.getProject()) != NONE);
    }
  }
}
