package com.jetbrains.cidr.cpp.embedded.platformio.ui;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle;
import org.jetbrains.annotations.NotNull;

public class PlatformioMonitorAction extends PlatformioAction {
  public PlatformioMonitorAction() {
    super(() -> ClionEmbeddedPlatformioBundle.message("task.monitor"),
          () -> ClionEmbeddedPlatformioBundle.message("task.monitor.description"));
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    actionPerformed(e, "-c clion device monitor", false, true);
  }
}
