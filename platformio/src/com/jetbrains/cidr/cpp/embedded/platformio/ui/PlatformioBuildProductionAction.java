package com.jetbrains.cidr.cpp.embedded.platformio.ui;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.cidr.cpp.embedded.platformio.ui.PlatformioActionBase.FUS_COMMAND.BUILD_PRODUCTION;

public class PlatformioBuildProductionAction extends PlatformioAction {
  public PlatformioBuildProductionAction() {super(() -> ClionEmbeddedPlatformioBundle.message("task.production"));}

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    actionPerformed(e, "-c clion run", false, true);
  }

  @Override
  public @NotNull FUS_COMMAND getFusCommand() {
    return BUILD_PRODUCTION;
  }
}
