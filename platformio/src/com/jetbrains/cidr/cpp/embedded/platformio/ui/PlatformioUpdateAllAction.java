package com.jetbrains.cidr.cpp.embedded.platformio.ui;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.cidr.cpp.embedded.platformio.ui.PlatformioActionBase.FUS_COMMAND.UPDATE_ALL;

public class PlatformioUpdateAllAction extends PlatformioAction {
  public PlatformioUpdateAllAction() {
    super(() -> ClionEmbeddedPlatformioBundle.message("task.update.all"),
          () -> ClionEmbeddedPlatformioBundle.message("platformio.update.description"));
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    actionPerformed(e, "-c clion update", true, false);
  }

  @Override
  public @NotNull FUS_COMMAND getFusCommand() {
    return UPDATE_ALL;
  }
}
