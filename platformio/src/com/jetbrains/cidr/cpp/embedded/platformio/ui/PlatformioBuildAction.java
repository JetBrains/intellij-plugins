package com.jetbrains.cidr.cpp.embedded.platformio.ui;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle;
import org.jetbrains.annotations.NotNull;

public class PlatformioBuildAction extends PlatformioAction {
  public PlatformioBuildAction() {super(() -> ClionEmbeddedPlatformioBundle.message("task.build"));}

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    actionPerformed(e, "-c clion run --target debug", false, true);
  }
}
