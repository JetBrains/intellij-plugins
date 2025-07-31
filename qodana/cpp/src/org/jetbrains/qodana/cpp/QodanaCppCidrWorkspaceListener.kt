package org.jetbrains.qodana.cpp

import com.intellij.util.PlatformUtils
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspaceReloadTask
import com.jetbrains.cidr.project.workspace.CidrWorkspace
import com.jetbrains.cidr.project.workspace.CidrWorkspaceListener

class QodanaCppCidrWorkspaceListener : CidrWorkspaceListener {
  override fun initialized(workspace: CidrWorkspace) {
    if (!PlatformUtils.isQodana()) return

    if (workspace is CMakeWorkspace) {
      workspace.scheduleReload(CMakeWorkspaceReloadTask.clearCache())
    }
  }
}