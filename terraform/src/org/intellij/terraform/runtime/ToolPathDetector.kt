// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.execution.wsl.WslPath
import com.intellij.openapi.diagnostic.fileLogger
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import org.intellij.terraform.install.TFToolType

interface ToolPathDetector {
  fun detectedPath(): String?
  val actualPath: String
  suspend fun detect(): Boolean
}

internal abstract class ToolPathDetectorBase(protected val project: Project, protected val coroutineScope: CoroutineScope, protected val toolType: TFToolType) : ToolPathDetector {

  private var detectedPath: String? = null

  override fun detectedPath(): String? = detectedPath

  override suspend fun detect(): Boolean {
    return withContext(Dispatchers.IO) {
      runInterruptible {
        val projectFilePath = project.projectFilePath
        if (projectFilePath != null) {
          val wslDistribution = WslPath.getDistributionByWindowsUncPath(projectFilePath)
          if (wslDistribution != null) {
            try {
              val out = wslDistribution.executeOnWsl(3000, "which", toolType.executableName)
              if (out.exitCode == 0) {
                detectedPath = wslDistribution.getWindowsPath(out.stdout.trim())
                return@runInterruptible true
              }
            }
            catch (e: Exception) {
              fileLogger().warn(e)
            }
          }
        }

        val binaryPath = PathEnvironmentVariableUtil.findInPath(toolType.getBinaryName())
        if (binaryPath != null && binaryPath.canExecute()) {
          detectedPath = binaryPath.absolutePath
          return@runInterruptible true
        }
        return@runInterruptible false
      }
    }
  }


}