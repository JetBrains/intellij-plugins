package com.intellij.dts.clion.impl

import com.intellij.dts.clion.DtsCLionUtil
import com.intellij.openapi.project.Project
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace

class DtsClionUtilImpl : DtsCLionUtil {
  override fun isCMakeAvailable(project: Project): Boolean {
    return CMakeWorkspace.getInstance(project).isInitialized
  }
}