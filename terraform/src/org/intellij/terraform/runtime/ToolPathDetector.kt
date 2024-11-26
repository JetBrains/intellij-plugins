// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.execution.wsl.WslPath
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import org.intellij.terraform.install.getBinaryName
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

@Service(Service.Level.PROJECT)
internal class ToolPathDetector(val project: Project, val coroutineScope: CoroutineScope) {

  companion object {
    fun getInstance(project: Project): ToolPathDetector = project.service<ToolPathDetector>()
  }

  suspend fun detect(path: String): String? {
    return withContext(Dispatchers.IO) {
      runInterruptible {
        if (isExecutable(path)) {
          return@runInterruptible path
        }
        val fileName = Path(path).fileName
        val projectFilePath = project.projectFilePath
        if (projectFilePath != null) {
          val wslDistribution = WslPath.getDistributionByWindowsUncPath(projectFilePath)
          if (wslDistribution != null) {
            try {
              val out = wslDistribution.executeOnWsl(3000, "which", fileName.nameWithoutExtension)
              if (out.exitCode == 0) {
                return@runInterruptible wslDistribution.getWindowsPath(out.stdout.trim())
              } else {
                logger<ToolPathDetector>().info("Cannot detect ${path} in WSL. Output stdout: ${out.stdout}")
                return@runInterruptible null
              }
            }
            catch (e: Exception) {
              logger<ToolPathDetector>().warnWithDebug(e)
            }
          }
        }
        return@runInterruptible findExecutable(getBinaryName(fileName.nameWithoutExtension))
      }
    }
  }

  private fun findExecutable(path: String): String? {
    return PathEnvironmentVariableUtil.findInPath(path)?.takeIf { file -> isExecutable(file.path) }?.absolutePath
  }

  fun isExecutable(path: String): Boolean {
    return File(path).canExecute()
  }
}