package com.intellij.dts.cmake

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project

interface DtsCMakeUtil {
  companion object {
    val EP_NAME: ExtensionPointName<DtsCMakeUtil> = ExtensionPointName.Companion.create<DtsCMakeUtil>("com.intellij.clion.dtsUtil")

    fun isCMakeAvailable(project: Project): Boolean {
      return EP_NAME.extensionList.any { it.isCMakeAvailable(project) }
    }
  }

  fun isCMakeAvailable(project: Project): Boolean
}