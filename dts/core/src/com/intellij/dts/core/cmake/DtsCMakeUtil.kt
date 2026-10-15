package com.intellij.dts.core.cmake

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project

interface DtsCMakeUtil {
  companion object {
    val EP_NAME: ExtensionPointName<DtsCMakeUtil> = ExtensionPointName.create<DtsCMakeUtil>("com.intellij.clion.dtsUtil")

    fun isCMakeAvailable(project: Project): Boolean {
      return EP_NAME.extensionList.any { it.isCMakeAvailable(project) }
    }
  }

  fun isCMakeAvailable(project: Project): Boolean
}