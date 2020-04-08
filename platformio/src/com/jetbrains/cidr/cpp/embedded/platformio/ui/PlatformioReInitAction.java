package com.jetbrains.cidr.cpp.embedded.platformio.ui;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle;
import org.jetbrains.annotations.NotNull;

public class PlatformioReInitAction extends PlatformioAction {
  public PlatformioReInitAction() {
    super(() -> ClionEmbeddedPlatformioBundle.message("action.name.reinit"),
          () -> ClionEmbeddedPlatformioBundle.message("action.name.reinit.description"));
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    actionPerformed(e, "-c clion init --ide clion", true, false);
  }
}
