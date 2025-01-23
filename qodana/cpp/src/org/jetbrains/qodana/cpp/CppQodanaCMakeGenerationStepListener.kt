package org.jetbrains.qodana.cpp

import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.PlatformUtils
import com.jetbrains.cidr.cpp.cmake.CMakeRunner.CMakeOutput
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspaceListener

class CppQodanaCMakeGenerationStepListener : CMakeWorkspaceListener {
  companion object {
    val LOG = Logger.getInstance(CppQodanaCMakeGenerationStepListener::class.java);
  }

  override fun generationCMakeExited(output: CMakeOutput) {
    if (!PlatformUtils.isQodana()) return

    if (output.exitCode != 0) {
      LOG.error(output.output.toString())
    }
  }
}