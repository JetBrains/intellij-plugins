// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.execution.wsl.WslPath
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.util.concurrency.annotations.RequiresEdt
import kotlinx.coroutines.*
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.install.TfToolType
import org.intellij.terraform.install.getBinaryName
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.nameWithoutExtension

@Service(Service.Level.PROJECT)
internal class ToolPathDetector(val project: Project, val coroutineScope: CoroutineScope) {

  companion object {
    fun getInstance(project: Project): ToolPathDetector = project.service<ToolPathDetector>()
  }

  @RequiresEdt
  fun detectPathAndUpdateSettingsIfEmpty(toolType: TfToolType) {
    toolType.getToolSettings(project).toolPath.ifBlank {
      runWithModalProgressBlocking(project, HCLBundle.message("progress.title.detecting.terraform.executable", toolType.displayName)) {
        detectPathAndUpdateSettingsAsync(toolType).await()
      }
    }
  }

  fun detectPathAndUpdateSettingsAsync(toolType: TfToolType): Deferred<TfToolSettings> {
    return coroutineScope.async {
      val settings = toolType.getToolSettings(project)
      val execName = toolType.executableName
      if (execName.isNotBlank()) {
        val detectedPath = detect(execName)
        if (!detectedPath.isNullOrEmpty()) {
          settings.toolPath = detectedPath
        }
      }
      settings
    }
  }

  suspend fun detect(path: String): String? {
    return withContext(Dispatchers.IO) {
      runInterruptible {
        val filePath = Path(path)
        if (Files.isExecutable(filePath)) {
          return@runInterruptible path
        }
        val fileName = filePath.fileName
        val projectFilePath = project.projectFilePath
        if (projectFilePath != null) {
          val wslDistribution = WslPath.getDistributionByWindowsUncPath(projectFilePath)
          if (wslDistribution != null) {
            try {
              val out = wslDistribution.executeOnWsl(3000, "which", fileName.nameWithoutExtension)
              if (out.exitCode == 0) {
                return@runInterruptible wslDistribution.getWindowsPath(out.stdout.trim())
              }
              else {
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
    return PathEnvironmentVariableUtil.findInPath(path)?.takeIf { file -> file.canExecute() }?.absolutePath
  }

  suspend fun isExecutable(path: String): Boolean {
    return withContext(Dispatchers.IO) {
      runInterruptible {
        val filePath = Path(path)
        if (Files.isExecutable(filePath)) return@runInterruptible true

        val wslDistribution = WslPath.getDistributionByWindowsUncPath(path)
        if (wslDistribution != null) {
          try {
            val command = wslDistribution.getWslPath(filePath)
            val out = wslDistribution.executeOnWsl(3000, "test", "-x", command)
            out.exitCode == 0
          }
          catch (e: Exception) {
            logger<ToolPathDetector>().warnWithDebug(e)
            false
          }
        }
        else {
          false
        }
      }
    }
  }
}