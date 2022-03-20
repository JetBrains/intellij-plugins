package com.jetbrains.cidr.cpp.embedded.platformio.ui;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.cidr.cpp.embedded.platformio.ui.PlatformioActionBase.FUS_COMMAND.INIT;

public class PlatformioReInitAction extends PlatformioAction {
  public PlatformioReInitAction() {
    super(() -> ClionEmbeddedPlatformioBundle.message("action.name.reinit"),
          () -> ClionEmbeddedPlatformioBundle.message("action.name.reinit.description"));
  }

  @Override
  public void actionPerformed(final @NotNull AnActionEvent e) {
    actionPerformed(e, "-c clion init --ide clion", true, false);
  }

  @Override
  public @NotNull FUS_COMMAND getFusCommand() {
    return INIT;
  }
}
