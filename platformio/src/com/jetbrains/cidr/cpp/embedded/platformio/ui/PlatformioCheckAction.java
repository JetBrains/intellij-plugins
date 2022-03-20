package com.jetbrains.cidr.cpp.embedded.platformio.ui;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.cidr.cpp.embedded.platformio.ui.PlatformioActionBase.FUS_COMMAND.CHECK;

public class PlatformioCheckAction extends PlatformioAction {
  public PlatformioCheckAction() {super(() -> ClionEmbeddedPlatformioBundle.message("task.check"));}

  @Override
  public void actionPerformed(final @NotNull AnActionEvent e) {
    actionPerformed(e, "-c clion check", false, true);
  }

  @Override
  public @NotNull FUS_COMMAND getFusCommand() {
    return CHECK;
  }
}
