package com.intellij.dts.cmake.impl

import com.intellij.dts.cmake.DtsCMakeUtil
import com.intellij.openapi.project.Project
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace

class DtsCMakeUtilImpl : DtsCMakeUtil {
  override fun isCMakeAvailable(project: Project): Boolean {
    return CMakeWorkspace.getInstance(project).isInitialized
  }
}