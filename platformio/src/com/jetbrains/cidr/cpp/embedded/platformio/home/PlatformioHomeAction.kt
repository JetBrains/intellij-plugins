package com.jetbrains.cidr.cpp.embedded.platformio.home

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle
import com.jetbrains.cidr.cpp.embedded.platformio.ui.PlatformioActionBase

class PlatformioHomeAction : PlatformioActionBase({ ClionEmbeddedPlatformioBundle.message("task.home") }, { "pio home" },
                                                  pioIcon(AllIcons.Nodes.HomeFolder)) {
  override fun actionPerformed(e: AnActionEvent) {
      actionPerformed(e, false, false, false, "home")
  }

}