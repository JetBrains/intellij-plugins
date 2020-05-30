package com.jetbrains.cidr.cpp.embedded.platformio.ui;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle;
import org.jetbrains.annotations.NotNull;

public class PlatformioUpdateAllAction extends PlatformioAction {
  public PlatformioUpdateAllAction() {
    super(() -> ClionEmbeddedPlatformioBundle.message("task.update.all"),
          () -> ClionEmbeddedPlatformioBundle.message("platformio.update.description"));
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    actionPerformed(e, "-c clion update", true, false);
  }
}
