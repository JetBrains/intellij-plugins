package com.jetbrains.cidr.cpp.embedded.platformio.ui;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.cidr.cpp.embedded.platformio.ui.PlatformioActionBase.FUS_COMMAND.BUILD;

public class PlatformioBuildAction extends PlatformioAction {
  public PlatformioBuildAction() {super(() -> ClionEmbeddedPlatformioBundle.message("task.build"));}

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    actionPerformed(e, "-c clion run --target debug", false, true);
  }

  @Override
  public @NotNull FUS_COMMAND getFusCommand() {
    return BUILD;
  }
}
