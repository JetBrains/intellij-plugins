package com.jetbrains.cidr.cpp.embedded.platformio.ui;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class PlatformioCheckAction extends PlatformioAction {
  public PlatformioCheckAction() {super("Check", null);}

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    actionPerformed(e, "-c clion check", false, true);
  }
}
