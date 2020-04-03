package com.jetbrains.cidr.cpp.embedded.platformio.ui;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class PlatformioHomeAction extends PlatformioActionBase {
  public PlatformioHomeAction() {super("Home", "Start PlatformIO internal web server");}

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    actionPerformed(e, "-c clion home", false, false);
  }
}
