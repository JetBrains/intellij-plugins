package com.jetbrains.cidr.cpp.embedded.platformio.ui;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle;
import org.jetbrains.annotations.NotNull;

public class PlatformioHomeAction extends PlatformioActionBase {
  public PlatformioHomeAction() {
    super(() -> ClionEmbeddedPlatformioBundle.message("task.home"), () -> ClionEmbeddedPlatformioBundle.message("task.home.description"));
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    actionPerformed(e, "-c clion home", false, false);
  }
}
