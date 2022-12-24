package com.jetbrains.cidr.cpp.embedded.platformio.project

import com.intellij.execution.ExecutionTarget
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.util.NlsSafe
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioDebugConfiguration
import com.jetbrains.cidr.cpp.execution.compound.CidrCompoundRunConfiguration
import icons.ClionEmbeddedPlatformioIcons
import javax.swing.Icon

class PlatformioExecutionTarget(@NlsSafe private val myId: String) : ExecutionTarget() {
  override fun getId(): String = this.myId

  override fun getDisplayName(): String = myId

  override fun getIcon(): Icon = ClionEmbeddedPlatformioIcons.Platformio
  override fun canRun(configuration: RunConfiguration): Boolean {
    return configuration is CidrCompoundRunConfiguration || configuration is PlatformioDebugConfiguration
  }
}