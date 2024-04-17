package com.jetbrains.cidr.cpp.embedded.platformio.ui

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioWorkspace

class PlatformioActionGroup : DefaultActionGroup(), DumbAware {

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabledAndVisible =
      e.project?.service<PlatformioWorkspace>()?.isInitialized == true
  }
}
