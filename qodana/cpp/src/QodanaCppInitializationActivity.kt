package org.jetbrains.qodana.cpp

import com.intellij.openapi.project.Project
import com.intellij.util.PlatformUtils
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace
import com.jetbrains.cidr.lang.workspace.RunAfterOCWorkspaceIsInitialized

class QodanaCppInitializationActivity : RunAfterOCWorkspaceIsInitialized {
  override fun runActivity(project: Project) {
    if (!PlatformUtils.isQodana()) return  // Only force a reload in a headless environment
    CMakeWorkspace.forceReloadOnOpening(project.baseDir)
  }
}