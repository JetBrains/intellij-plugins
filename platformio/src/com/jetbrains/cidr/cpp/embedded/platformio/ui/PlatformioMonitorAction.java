package com.jetbrains.cidr.cpp.embedded.platformio.ui;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class PlatformioMonitorAction extends PlatformioAction {
  public PlatformioMonitorAction() {super("Monitor", null);}

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    actionPerformed(e, "-c clion device monitor", false, true);
  }
}
