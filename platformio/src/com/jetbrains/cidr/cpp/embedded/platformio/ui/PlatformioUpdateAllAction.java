package com.jetbrains.cidr.cpp.embedded.platformio.ui;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class PlatformioUpdateAllAction extends PlatformioAction {
  public PlatformioUpdateAllAction() {super("Update All", "Update platforms, toolchains etc");}

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    actionPerformed(e, "-c clion update", true, false);
  }
}
