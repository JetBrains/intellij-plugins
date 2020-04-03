package com.jetbrains.cidr.cpp.embedded.platformio.ui;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class PlatformioBuildProductionAction extends PlatformioAction {
  public PlatformioBuildProductionAction() {super("Build Production", null);}

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    actionPerformed(e, "-c clion run", false, true);
  }
}
