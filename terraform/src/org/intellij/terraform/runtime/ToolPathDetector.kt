// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.platform.eel.provider.asNioPath
import com.intellij.platform.eel.provider.getEelDescriptor
import com.intellij.platform.eel.provider.utils.where
import com.intellij.platform.ide.progress.withBackgroundProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.intellij.terraform.config.TfConstants
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.install.TfToolType
import java.nio.file.Path
import kotlin.io.path.*

internal interface ToolPathDetector {

  companion object {
    fun getInstance(project: Project): ToolPathDetector = project.service<ToolPathDetector>()
  }

  suspend fun detectAndVerifyTool(toolType: TfToolType, overrideExistingValue: Boolean): Boolean {
    return true
  }

  fun isExecutable(path: Path): Boolean {
    return true
  }

  suspend fun detect(path: String): String?
}

internal class ToolPathDetectorImpl(val project: Project, val coroutineScope: CoroutineScope) : ToolPathDetector {

  override suspend fun detectAndVerifyTool(toolType: TfToolType, overwriteExistingSettings: Boolean): Boolean {
    if (overwriteExistingSettings || toolType.getToolSettings(project).toolPath.isBlank()) {
      withBackgroundProgress(project, HCLBundle.message("progress.title.detecting.terraform.executable", toolType.displayName)) {
        detectToolAndUpdateSettings(toolType)
      }
    }
    return isExecutable(Path(toolType.getToolSettings(project).toolPath))
  }

  private suspend fun detectToolAndUpdateSettings(toolType: TfToolType): TfToolSettings {
    val settings = toolType.getToolSettings(project)
    val execName = toolType.executableName
    if (execName.isNotBlank()) {
      val detectedPath = detect(execName)
      if (!detectedPath.isNullOrEmpty()) {
        settings.toolPath = detectedPath
      }
    }
    return settings
  }

  override suspend fun detect(path: String): String? {
    return withContext(Dispatchers.IO) {
      val filePath = Path(path)
      if (isExecutable(filePath)) {
        return@withContext path
      }
      val fileName = filePath.fileName.nameWithoutExtension
      val eelApi = project.getEelDescriptor().upgrade()
      val exePath = eelApi.exec.where(fileName)?.asNioPath()?.takeIf { isExecutable(it) }?.absolutePathString()
      return@withContext exePath
    }
  }

  override fun isExecutable(path: Path): Boolean {
    return path.pathString.isNotBlank() && path.isRegularFile() && path.isExecutable()
  }
}

internal fun showIncorrectPathNotification(
  project: Project,
  toolType: TfToolType,
) {
  val toolPath = toolType.getToolSettings(project).toolPath
  TfConstants.getNotificationGroup().createNotification(
    HCLBundle.message("run.configuration.terraform.path.title", toolType.displayName),
    HCLBundle.message("run.configuration.terraform.path.incorrect", toolPath.ifEmpty { toolType.executableName }, toolType.displayName),
    NotificationType.ERROR
  ).addAction(OpenSettingsAction()).notify(project)
}

internal class OpenSettingsAction : NotificationAction(HCLBundle.message("terraform.open.settings")) {
  override fun actionPerformed(e: AnActionEvent, notification: Notification) {
    notification.expire()
    ShowSettingsUtil.getInstance().showSettingsDialog(e.project, TfToolConfigurable::class.java)
  }
}

