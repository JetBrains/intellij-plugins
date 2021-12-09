package com.jetbrains.cidr.cpp.embedded.platformio.ui;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.cidr.cpp.embedded.platformio.ui.PlatformioActionBase.FUS_COMMAND.MONITOR;

public class PlatformioMonitorAction extends PlatformioAction {
  public PlatformioMonitorAction() {
    super(() -> ClionEmbeddedPlatformioBundle.message("task.monitor"),
          () -> ClionEmbeddedPlatformioBundle.message("task.monitor.description"));
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    actionPerformed(e, "-c clion device monitor", false, true);
  }

  @Override
  public @NotNull FUS_COMMAND getFusCommand() {
    return MONITOR;
  }
}
