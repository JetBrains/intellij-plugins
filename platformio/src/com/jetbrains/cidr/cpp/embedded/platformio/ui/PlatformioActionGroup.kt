package com.jetbrains.cidr.cpp.embedded.platformio.ui

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.components.service
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle
import com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioWorkspace
import icons.ClionEmbeddedPlatformioIcons

class PlatformioActionGroup : ActionGroup(ClionEmbeddedPlatformioBundle.message("platformio.actiongroup.name"),
                                          ClionEmbeddedPlatformioBundle.message("platformio.support"),
                                          ClionEmbeddedPlatformioIcons.Platformio) {
  override fun getChildren(e: AnActionEvent?): Array<AnAction> {
    return getChildren(e, ActionManager.getInstance())
  }

  override fun getChildren(e: AnActionEvent?, manager: ActionManager): Array<AnAction> {
    return (manager.getAction("platformio-group") as ActionGroup).getChildren(e, manager)
  }

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabledAndVisible =
      e.project?.service<PlatformioWorkspace>()?.isInitialized ?: false
  }

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun isDumbAware(): Boolean = true
}
