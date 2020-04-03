package com.jetbrains.cidr.cpp.embedded.platformio.ui;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class PlatformioReInitAction extends PlatformioAction {
  public PlatformioReInitAction() {super("Re-Init", "(Re)initialize project & CMake");}

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    actionPerformed(e, "-c clion init --ide clion", true, false);
  }
}
