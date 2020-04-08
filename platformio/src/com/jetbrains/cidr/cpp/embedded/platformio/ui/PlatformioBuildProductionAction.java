package com.jetbrains.cidr.cpp.embedded.platformio.ui;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle;
import org.jetbrains.annotations.NotNull;

public class PlatformioBuildProductionAction extends PlatformioAction {
  public PlatformioBuildProductionAction() {super(() -> ClionEmbeddedPlatformioBundle.message("task.production"));}

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    actionPerformed(e, "-c clion run", false, true);
  }
}
