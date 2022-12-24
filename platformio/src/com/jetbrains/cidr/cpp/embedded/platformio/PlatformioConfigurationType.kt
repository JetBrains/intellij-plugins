package com.jetbrains.cidr.cpp.embedded.platformio

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NotNullLazyValue
import icons.ClionEmbeddedPlatformioIcons

class PlatformioConfigurationType : ConfigurationTypeBase(TYPE_ID, ClionEmbeddedPlatformioBundle.message("platformio.name"),
                                                          ClionEmbeddedPlatformioBundle.message("platformio.description"),
                                                          NotNullLazyValue.createValue { ClionEmbeddedPlatformioIcons.Platformio }), DumbAware {
  val factory: ConfigurationFactory =
    object : ConfigurationFactory(this) {
      override fun getId(): String = PLATFORM_IO_DEBUG_ID

      override fun createTemplateConfiguration(project: Project): RunConfiguration =
        PlatformioDebugConfiguration(project, this)
    }

  init {
    addFactory(factory)
  }

  override fun getHelpTopic(): String = "rundebugconfigs.platformio"

  companion object {
    const val PLATFORM_IO_DEBUG_ID = "PlatformIO Debug"
    const val TYPE_ID = "platformio"
  }
}